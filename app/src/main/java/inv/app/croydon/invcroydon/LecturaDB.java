package inv.app.croydon.invcroydon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class LecturaDB {

    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private Cursor cursor;

    public LecturaDB(Context context) {
        dbHelper = new DBHelper(context);
    }

    private void openReadDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void CloseDB() {
        if (db != null)
            db.close();
    }


    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, ConstatesDB.DB_NAME, null, ConstatesDB.DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(ConstatesDB.TABLA_LECTURAS_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //db.execSQL(ConstatesDB.TABLA_LECTURAS_SQL);
        }
    }

    private ContentValues LecturaMappeContendValues(Lecturas lecturas) {
        ContentValues Cv = new ContentValues();
        Cv.put(ConstatesDB.LECT_FECHA, lecturas.getFecha());
        Cv.put(ConstatesDB.LECT_BOD, lecturas.getBodega());
        Cv.put(ConstatesDB.LECT_AREA, lecturas.getArea());
        Cv.put(ConstatesDB.LECT_EST, lecturas.getEstante());
        Cv.put(ConstatesDB.LECT_SELEC, lecturas.getSeleccion());
        Cv.put(ConstatesDB.LECT_CONT, lecturas.getConteo());
        Cv.put(ConstatesDB.LECT_EAN, lecturas.getEan());
        Cv.put(ConstatesDB.LECT_UMED, lecturas.getUndMed());
        Cv.put(ConstatesDB.LECT_CANT, lecturas.getCantidad());
        Cv.put(ConstatesDB.LECT_FCONT, lecturas.getFechaConteo());
        Cv.put(ConstatesDB.LECT_HORA, lecturas.getHoraConteo());
        return Cv;
    }

    public long insertLectura(Lecturas lecturas) {
        this.openWriteDB();
        long rowID = db.insert(ConstatesDB.TABLA_LECTURAS, null,
                LecturaMappeContendValues(lecturas));
        this.CloseDB();
        return rowID;
    }

    public void updateLectura(Lecturas lecturas) {
        this.openWriteDB();
        String query = ConstatesDB.LECT_EAN + " = ? and " + ConstatesDB.LECT_EST + " = ?";
        db.update(ConstatesDB.TABLA_LECTURAS, LecturaMappeContendValues(lecturas), query,
                new String[]{String.valueOf(lecturas.getEan()),
                        String.valueOf(lecturas.getEstante())});
        this.CloseDB();
    }

    public void deleteLectura(int id) {
        this.openWriteDB();
        String query = ConstatesDB.LECT_EAN + "= ?";
        db.delete(ConstatesDB.TABLA_LECTURAS, query, new String[]{String.valueOf(id)});
        this.CloseDB();
    }

    public ArrayList loadLectura() {
        ArrayList list = new ArrayList<>();

        this.openReadDB();
        String[] Registros = new String[]{ConstatesDB.LECT_FECHA, ConstatesDB.LECT_BOD,
                ConstatesDB.LECT_AREA, ConstatesDB.LECT_EST, ConstatesDB.LECT_SELEC,
                ConstatesDB.LECT_CONT, ConstatesDB.LECT_EAN, ConstatesDB.LECT_UMED,
                ConstatesDB.LECT_CANT, ConstatesDB.LECT_FCONT, ConstatesDB.LECT_HORA};
        Cursor cursor = db.query(ConstatesDB.TABLA_LECTURAS, Registros, null,
                null, null, null, null);
        try {
            while (cursor.moveToNext()) {
                Lecturas lecturas = new Lecturas();
                lecturas.setFecha(cursor.getInt(0));
                lecturas.setBodega(cursor.getInt(1));
                lecturas.setArea(cursor.getInt(2));
                lecturas.setEstante(cursor.getString(3));
                lecturas.setSeleccion(cursor.getInt(4));
                lecturas.setConteo(cursor.getInt(5));
                lecturas.setEan(cursor.getString(6));
                lecturas.setUndMed(cursor.getInt(7));
                lecturas.setCantidad(cursor.getInt(8));
                lecturas.setFechaConteo(cursor.getString(9));
                lecturas.setHoraConteo(cursor.getInt(10));
            }
        } finally {
            cursor.close();
        }
        cursor.close();
        this.CloseDB();
        return list;
    }

    public boolean ExisteTabla() {
        boolean exist = false;
        this.openReadDB();
        String query = "select DISTINCT * from sqlite_master where tbl_name = '" + "Lecturas" + "'";
        Cursor c = db.rawQuery(query, null);
        if (c != null) {
            if (c.getCount() > 0) {
                exist = true;
                c.close();
            }
        }
        this.CloseDB();
        return exist;
    }

    public void CreateTable() {
        db.execSQL(ConstatesDB.TABLA_LECTURAS_SQL);
    }

    public int buscarLectura(String EAN, String Estante) {
        int lecturas = 0;
        this.openReadDB();
        Cursor c = db.rawQuery("select * from " + ConstatesDB.TABLA_LECTURAS + " where " +
                ConstatesDB.LECT_EAN + "='" + EAN + "'" + " and " + ConstatesDB.LECT_EST + "='" +
                Estante + "'", null);
        if (c != null) {
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    lecturas = c.getInt(c.getColumnIndex(ConstatesDB.LECT_CANT));
                }
            }
        }
        c.close();
        this.CloseDB();
        return lecturas;
    }


    public boolean TablaVacia() {
        boolean vacia = true;
        this.openReadDB();
        Cursor c = db.rawQuery("SELECT count(*) FROM " + ConstatesDB.TABLA_LECTURAS,
                null);
        c.moveToFirst();
        if (c.getInt(0) > 0) {
            vacia = false;
        }
        c.close();
        this.CloseDB();
        return vacia;
    }

    public boolean ExportDataBase(File file) {
        boolean exportFile = false;
        try {
            this.openReadDB();
            File File2 = file;
            FileWriter writer = new FileWriter(File2, true);

            Cursor cursor = db.rawQuery("SELECT * FROM " + ConstatesDB.TABLA_LECTURAS,
                    null);
            while (cursor.moveToNext()) {
                String CantEdi = cursor.getString(8);
                String EstEdi = cursor.getString(3);
                String AreaEdi = cursor.getString(2);
                for (int i = CantEdi.length(); i < 6; i++) {
                    CantEdi = "0" + CantEdi;
                }
                for (int i = EstEdi.length(); i < 3; i++) {
                    EstEdi = "0" + EstEdi;
                }
                for (int i = AreaEdi.length(); i < 2; i++) {
                    AreaEdi = "0" + AreaEdi;
                }
                String RegistroLect =
                        cursor.getString(0) + ";" +
                                cursor.getString(1) + ";" +
                                AreaEdi + ";" +
                                EstEdi + ";" +
                                cursor.getString(4) + ";" +
                                cursor.getString(5) + ";" +
                                cursor.getString(6) + ";" +
                                cursor.getString(7) + ";" +
                                CantEdi + ";" +
                                cursor.getString(9) + ";" +
                                cursor.getString(10);
                writer.append(RegistroLect + "\r\n");
            }
            this.CloseDB();
            cursor.close();
            writer.flush();
            writer.close();
            exportFile = true;
        } catch (Exception o) {
            this.CloseDB();
        }
        return exportFile;
    }

    public void DeleteTableData() {
        this.openReadDB();
        db.execSQL("delete from " + ConstatesDB.TABLA_LECTURAS);
        this.CloseDB();
    }

    public String[] ExisteEstanteNivel(String estanteNiv, String Area) {
        String[] Ean = null;

        this.openReadDB();
        Cursor c = db.rawQuery("select * from " + ConstatesDB.TABLA_LECTURAS + " where " +
                ConstatesDB.LECT_EST + "='" + estanteNiv + "'" + " and " + ConstatesDB.LECT_AREA +
                "='" + Area + "'", null);

        if (c != null) {
            if (c.getCount() > 0) {
                Ean = new String[c.getCount()];
                int i = 0;
                while (c.moveToNext()) {
                    Ean[i] = c.getString(c.getColumnIndex(ConstatesDB.LECT_EAN)) + " = " +
                            c.getString(c.getColumnIndex(ConstatesDB.LECT_CANT));
                    i++;
                }
            }
        }
        c.close();
        this.CloseDB();
        return Ean;
    }

    public boolean BorrarEstanteNivel(String estanteNiv, String[] Area){
        boolean borrado = false;
        this.openWriteDB();
        int delete = 0;
        for (String Ean: Area) {
            String EAN = Ean.substring(0,13);
            String Query = ConstatesDB.LECT_EST + "=? and " + ConstatesDB.LECT_EAN + "=?";
            delete = delete + db.delete(ConstatesDB.TABLA_LECTURAS, Query, new String[]{
                    String.valueOf(estanteNiv),
                    String.valueOf(EAN)});
        }
        this.CloseDB();
       return  borrado;
    }
}
