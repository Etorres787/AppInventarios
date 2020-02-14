package inv.app.croydon.invcroydon;

public class ConstatesDB {
    public static final String DB_NAME = "inventarioscroydon.db";
    public static final int DB_VERSION = 1;


    //Tabla Lecturas
    public static final String TABLA_LECTURAS = "Lecturas";

    public static final String LECT_FECHA = "fec_lect";
    public static final String LECT_BOD = "bodega";
    public static final String LECT_AREA = "area";
    public static final String LECT_EST = "estante";
    public static final String LECT_SELEC = "seleccion";
    public static final String LECT_CONT = "conteo";
    public static final String LECT_EAN = "ean";
    public static final String LECT_UMED = "undmed";
    public static final String LECT_CANT = "cantidad";
    public static final String LECT_FCONT = "f_conteo";
    public static final String LECT_HORA = "h_conteo";


    public static final String TABLA_LECTURAS_SQL =
            "CREATE TABLE " +
                    TABLA_LECTURAS + "(" +
                    LECT_FECHA + " INTEGER NOT NULL," +
                    LECT_BOD + " INTEGER NOT NULL," +
                    LECT_AREA + " INTEGER NOT NULL," +
                    LECT_EST + " STRING NOT NULL," +
                    LECT_SELEC + " INTEGER NOT NULL," +
                    LECT_CONT + " INTEGER NOT NULL," +
                    LECT_EAN + "  STRING NOT NULL," +
                    LECT_UMED + " INTEGER NOT NULL," +
                    LECT_CANT + " INTEGER NOT NULL," +
                    LECT_FCONT + " TEXT NOT NULL," +
                    LECT_HORA + " INTEGER NOT NULL, PRIMARY KEY(" + LECT_EST + " , " + LECT_EAN + "));";
    public static final String Lect_N = "";
}
