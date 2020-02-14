package inv.app.croydon.invcroydon;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.DatagramSocketClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class Inventarios extends AppCompatActivity {

    private Spinner Calidad, Conteo, Medida;
    private EditText FechaInv, Ean_text, Estante_Text, Area_Text, TotEsxt_Text,
            Total_Text, PreEanRead;
    private TextView Area, Estante;
    private Button set_button, export_button, new_button;
    private String NameFile, ConteoLetra = "P";
    private File root, gpxfile, ServerFile;

    public ToneGenerator TonError;
    private Vibrator vibrator;
    private TextWatcher Twtch, estante_change;

    private List<String> ListEan;
    private int TotEstante = 0, TotalGlobal = 0, Bodegas = 16;

    private AlertDialog.Builder builder;

    public static final String TAG = "INV-NAME";

    Calendar myCalendar = Calendar.getInstance();

    public BluetoothAdapter mBluetoothAdapter;

    private String BluetoohName = null;

    public FTPClient FtpCroydon, Ftp;

    private LecturaDB db;
    private Lecturas lecturas;
    private Switch TipPos;
    private boolean EsTarjeta = true, configfecha = false, PrimerEstante = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inventarios);

        //Solicitud de permisos de almacenamiento
        if (!Check_STORAGE(this)) {
            Request_STORAGE(this, 1);
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        BluetoohName = mBluetoothAdapter.getName();
        if (BluetoohName == null) {
            BluetoohName = mBluetoothAdapter.getAddress();
        }

        db = new LecturaDB(this);

        boolean tablaexiste = db.ExisteTabla();

        if (tablaexiste == false) {
            db.CreateTable();
        }

        MapingFormulario();
        Ean_text.setInputType(InputType.TYPE_NULL);
        Area = (TextView) findViewById(R.id.textView7);
        Estante = (TextView) findViewById(R.id.textView6);
        Area.setText("Area");
        Estante.setText("Estante");

        //escucha el cambio del selector
        TipPos.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    TipPos.setText("Sticker");
                    Area.setText("Pasillo");
                    Estante.setText("Estante-Nivel");
                    SetStikerInv();
                    EsTarjeta = false;
                } else {
                    TipPos.setText("Tarjeta");
                    Area.setText("Area");
                    Estante.setText("Estante");
                    ClearFormulario();
                }
            }
        });

        //Instanciamiento de campos formulario
        TonError = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //seleccionar fecha
        FechaInv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(Inventarios.this, date, myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).
                        show();
            }
        });

        ListCalidad();

        //Exportar archivo plano
        export_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db.TablaVacia() == false) {
                    ExportDatabase();
                    ClearFormulario();
                }else{
                    DispDinamicAlert("Alerta","No hay datos para exportar!");
                }
            }
        });

        new_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (db.TablaVacia() == false) {
                    DisplayAlert();
                }else{
                    ClearFormulario();
                }
            }
        });


        //Boton SET habilita EAN_text
        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ready = Setisready();
                if (ready == true) {
                    TipPos.setEnabled(false);
                    Ean_text.setEnabled(true);
                    Calidad.setEnabled(false);
                    Conteo.setEnabled(false);
                    Area_Text.setEnabled(false);
                    TotEsxt_Text.setEnabled(false);
                    Total_Text.setEnabled(false);
                    FechaInv.setEnabled(false);
                    set_button.setEnabled(false);
                    new_button.setEnabled(true);
                    export_button.setEnabled(true);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
                    Date now = new Date();
                    String fileNamea = formatter.format(now);//like 2016_01_12.txt

                    try {
                        Calendar HoraAhora = Calendar.getInstance();
                        int intNameFile = HoraAhora.get(Calendar.HOUR);

                        if (BluetoohName == null) {
                            BluetoohName = "No_define_name";
                        }

                        NameFile = BluetoohName + "_" + Integer.toString(HoraAhora.get
                                (Calendar.HOUR_OF_DAY)) +
                                Integer.toString(HoraAhora.get(Calendar.MINUTE)) +
                                Integer.toString(HoraAhora.get(Calendar.SECOND)) + ".txt";

                        root = new File(Environment.getExternalStorageDirectory() +
                                File.separator + "Documents", "Conteos_" + fileNamea);
                        //File root = new File(Environment.getExternalStorageDirectory(), "Notes");


                        if (!root.exists()) {
                            root.mkdirs();

                        }
                        root.setExecutable(true);
                        root.setReadable(true);
                        root.setWritable(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Ean_text.requestFocus();
                }
            }
        });

        //Cambio de estante evento
        Estante_Text.addTextChangedListener(estante_change = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //No se necesita
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //No se necesita
            }

            @Override
            public void afterTextChanged(Editable s) {
                Estante_Text.removeTextChangedListener(this);
                TotEstante = 0;
                TotEsxt_Text.setText("0");
                Estante_Text.addTextChangedListener(this);
                ListEan = new ArrayList<String>();
            }
        });
        //*******************************************************


        //LECTURA DE EAN 13 POR LINEA
        Ean_text.addTextChangedListener(Twtch = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PreEanRead.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {
                PreEanRead.setText(s);
                if (EsTarjeta == false) {
                    if (FechaInv.getText().toString().matches("")) {
                        DispDinamicAlert("Fecha", "Ingrese fecha de inventario!");
                        Ean_text.removeTextChangedListener(Twtch);
                        Ean_text.setText("");
                        Ean_text.addTextChangedListener(Twtch);
                        FechaInv.requestFocus();
                        VibraSuena();
                    }else{
                        configfecha = true;
                        FechaInv.setEnabled(false);

                        if (Islength7() == true) {
                            String[] UbicacionEan = db.ExisteEstanteNivel(
                                    Ean_text.getText().toString().substring(4, 7),
                                    Ean_text.getText().toString().substring(2, 4));
                            Conteo.setEnabled(false);
                            Calidad.setEnabled(false);
                            if(UbicacionEan != null){
                                String UbDelete = "Pasillo: " + Ean_text.getText().toString().substring(2, 4) +
                                        " Nivel: " + Ean_text.getText().toString().substring(4, 7);
                                AlertaBorrarUbicacion(UbicacionEan, UbDelete, (Ean_text.getText().toString()));
                                Ean_text.removeTextChangedListener(Twtch);
                                Ean_text.setText("");
                                Ean_text.addTextChangedListener(Twtch);
                            }else {
                                Bodegas = Integer.parseInt(Ean_text.getText().toString().substring(0, 2));
                                Area_Text.setText(Ean_text.getText().toString().substring(2, 4));
                                Estante_Text.setText(Ean_text.getText().toString().substring(4, 7));
                                TipPos.setEnabled(false);
                                Ean_text.removeTextChangedListener(Twtch);
                                Ean_text.setText("");
                                Ean_text.addTextChangedListener(Twtch);
                            }
                        }else {
                            if (Islength13() == true && PrimerEstante == true) {
                                AddToDatabase(Ean_text.getText().toString());
                                Ean_text.removeTextChangedListener(Twtch);
                                Ean_text.setText("");
                                Ean_text.addTextChangedListener(Twtch);
                                TotEstante++;
                                TotalGlobal++;
                                Total_Text.setText(Integer.toString(TotalGlobal));
                                TotEsxt_Text.setText(Integer.toString(TotEstante));
                            } else {
                                if(PrimerEstante == false) {
                                    Ean_text.removeTextChangedListener(this);
                                    Ean_text.setText("");
                                    Ean_text.addTextChangedListener(this);
                                    DispDinamicAlert("Alerta", "No ha leido primera ubicacion!");
                                    VibraSuena();
                                }
                                else{
                                    Ean_text.removeTextChangedListener(this);
                                    Ean_text.setText("");
                                    Ean_text.addTextChangedListener(this);
                                    DispDinamicAlert("Alerta", "No se reconoce EAN!");
                                    VibraSuena();
                                }
                            }
                        }
                    }
                } else {
                    if (Islength13() == true) {
                        AddToDatabase(Ean_text.getText().toString());
                        Ean_text.removeTextChangedListener(Twtch);
                        Ean_text.setText("");
                        Ean_text.addTextChangedListener(Twtch);
                        TotEstante++;
                        TotalGlobal++;
                        Total_Text.setText(Integer.toString(TotalGlobal));
                        TotEsxt_Text.setText(Integer.toString(TotEstante));
                    } else {
                        Ean_text.removeTextChangedListener(this);
                        Ean_text.setText("");
                        Ean_text.addTextChangedListener(this);
                        DispDinamicAlert("Alerta", "No se reconoce EAN!");
                        VibraSuena();
                    }
                }
            }
        });

        if (db.TablaVacia() == false) {
            DisplayAlert();
        }
    }

    public void SetStikerInv() {
        TotalGlobal = 0;
        TotEstante = 0;
        PreEanRead.setText("");
        PreEanRead.setEnabled(false);
        Conteo.setEnabled(true);
        FechaInv.setText("");
        Ean_text.setEnabled(true);
        Ean_text.requestFocus();
        Ean_text.setText("");
        Ean_text.setInputType(InputType.TYPE_NULL);
        set_button.setEnabled(false);
        Estante_Text.setText("");
        Estante_Text.setEnabled(false);
        Area_Text.setText("");
        Area_Text.setEnabled(false);
        TotEsxt_Text.setText("");
        TotEsxt_Text.setEnabled(false);
        Total_Text.setText("");
        Total_Text.setEnabled(false);
        export_button.setEnabled(true);
        new_button.setEnabled(true);

    }

    public void MapingFormulario() {
        TipPos = (Switch) findViewById(R.id.tippos);
        FechaInv = (EditText) findViewById(R.id.FechaInv);
        Ean_text = (EditText) findViewById(R.id.Ean_text);
        set_button = findViewById(R.id.set_button);
        export_button = findViewById(R.id.export_button);
        new_button = findViewById(R.id.new_button);
        Estante_Text = (EditText) findViewById(R.id.Estante_Text);
        Area_Text = (EditText) findViewById(R.id.Area_Text);
        TotEsxt_Text = (EditText) findViewById(R.id.TotEsxt_Text);
        Total_Text = (EditText) findViewById(R.id.Total_Text);
        PreEanRead = (EditText) findViewById(R.id.Pre_Ean);
        TotEsxt_Text.setEnabled(false);
        Total_Text.setEnabled(false);
        Ean_text.requestFocus();
        export_button.setEnabled(false);
        new_button.setEnabled(false);

    }

    public void ClearFormulario() {
        TotalGlobal = 0;
        TotEstante = 0;
        set_button.setEnabled(true);
        PreEanRead.setText("");
        PreEanRead.setEnabled(false);
        TipPos.setEnabled(true);
        TipPos.setChecked(false);
        FechaInv.setText("");
        FechaInv.setEnabled(true);
        Ean_text.setEnabled(false);
        set_button.setEnabled(true);
        Estante_Text.setText("");
        Estante_Text.setEnabled(true);
        Area_Text.setText("");
        Area_Text.setEnabled(true);
        TotEsxt_Text.setText("");
        TotEsxt_Text.setEnabled(false);
        Total_Text.setText("");
        Total_Text.setEnabled(false);
        export_button.setEnabled(false);
        new_button.setEnabled(false);
        Estante_Text.requestFocus();
        Conteo.setEnabled(true);
        Calidad.setEnabled(true);
    }

    public static boolean ValidaEanCroydon(String EAN) {
        boolean EsReferencia = false;

        try {
            URL url = new URL(EAN);
            HttpURLConnection conection = (HttpURLConnection) url.openConnection();
            StringBuilder Sb = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conection.getInputStream()));
            String Response;
            while ((Response = bufferedReader.readLine()) != null) {
                Sb.append(Response + "\n");
            }
        } catch (Exception f) {
            EsReferencia = false;
        }

        return EsReferencia;
    }

    public static boolean Check_STORAGE(Activity act) {
        int result = ContextCompat.checkSelfPermission(act,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }


    public static void Request_STORAGE(Activity act, int code) {
        ActivityCompat.requestPermissions(act, new
                String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, code);
    }


    //Llena opciones de calidad y numero de conteo
    public void ListCalidad() {
        Conteo = (Spinner) findViewById(R.id.Conteo);
        Calidad = (Spinner) findViewById(R.id.Calidad);
        Medida =(Spinner) findViewById(R.id.UndEmpaque);

        List<String> listado = new ArrayList<String>();
        listado.add("Primera");
        listado.add("Segunda");

        ArrayAdapter<String> listadapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listado);
        listadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Calidad.setAdapter(listadapter);

        List<String> listCont = new ArrayList<String>();
        listCont.add("Primer");
        listCont.add("Segundo");
        listCont.add("Tercer");
        ArrayAdapter<String> ContAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listCont);
        ContAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Conteo.setAdapter(ContAdapter);

        List<String> ListMed = new ArrayList<>();
        ListMed.add("Unidad");
        ListMed.add("Caja");
        ArrayAdapter<String> MedAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ListMed);
        MedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Medida.setAdapter(MedAdapter);
    }
    //****************************************

    //Seleccion de fecha de inventario
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override

        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            RefreshImput();
        }
    };

    private void RefreshImput() {
        String FormatoDeFecha = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(FormatoDeFecha);
        FechaInv.setText(sdf.format(myCalendar.getTime()));
    }
    //*******************************************

    private boolean Setisready() {
        boolean Ready = false;
        if (Estante_Text.getText().toString().matches("")) {
            Ready = false;
            Toast.makeText(this, "Ingrese estante de conteo!", Toast.LENGTH_SHORT).show();
        } else {
            Ready = true;
        }

        if (Area_Text.getText().toString().matches("")) {
            Ready = false;
            Toast.makeText(this, "Ingrese area de conteo!", Toast.LENGTH_SHORT).show();
        } else {
            Ready = true;
        }
        if (FechaInv.getText().toString().matches("")) {
            Ready = false;
            Toast.makeText(this, "Ingrese fecha de inventario!", Toast.LENGTH_SHORT).
                    show();
        } else {
            Ready = true;
        }
        return Ready;
    }

    //******************************************************************************
    public boolean Islength13() {
        boolean length = false;
        if (Ean_text.getText().toString().matches("")) {

        } else {
            if (Ean_text.getText().toString().length() >= 13) {
                length = true;
            } else {
                TonError.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500);
                vibrator.vibrate(500);
            }
        }
        return length;
    }

    public boolean Islength7() {
        boolean length = false;
        if (Ean_text.getText().toString().matches("")) {

        } else {
            if (Ean_text.getText().toString().length() == 7) {
                length = true;
                PrimerEstante = true;
            }
        }
        return length;
    }

    //Carga archivos al FTP para enviarlos a AURORA
    class loadFileInv extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            FTPClient ftpClient = new FTPClient();

            String FileLoad = root + "/" + NameFile;
            boolean Status = false;
            try {
                ftpClient.connect("190.143.76.156", 50006);
                ftpClient.login("german", "Tiendas*2018.");
                boolean asdasd = ftpClient.isAvailable();
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                BufferedInputStream buffer = null;
                FileInputStream srcFileStream = new FileInputStream(FileLoad);
                ftpClient.changeWorkingDirectory("/backup_tiendas_pos/edwin/");
                FTPFile[] ftpFiles = ftpClient.listDirectories();
                ftpClient.enterLocalPassiveMode();
                Status = ftpClient.storeFile(NameFile, srcFileStream);
                srcFileStream.close();
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException p) {
                String lol = p.getMessage();
            }

            return Status;
        }
    }

    public void AddToDatabase(String EANCODE) {

        Calendar HoraPlano = Calendar.getInstance();
        int IntCalid = 0, intCont = 0, UnMed = 2;
        String Cali = Calidad.getSelectedItem().toString();
        String Cont = Conteo.getSelectedItem().toString();
        String UMed = Medida.getSelectedItem().toString();

        switch (UMed){
            case "Caja":
                UnMed = 1;
                break;
            case "Unidad":
                UnMed = 2;
                break;
        }

        switch (Cali) {
            case "Primera":
                IntCalid = 1;
                break;
            case "Segunda":
                IntCalid = 2;
                break;
        }

        switch (Cont) {
            case "Primer":
                intCont = 1;
                ConteoLetra = "P";
                break;
            case "Segundo":
                intCont = 2;
                ConteoLetra = "S";
                break;
            case "Tercer":
                intCont = 3;
                ConteoLetra = "T";
                break;
        }

        String Area = "", Estante = "", Cantidad = "", Hora = "", Minuto = "";
        if (Area_Text.getText().toString().length() == 1)
            Area = "0" + Area_Text.getText().toString();
        if (Area_Text.getText().toString().length() == 2)
            Area = Area_Text.getText().toString();

        if (Estante_Text.getText().toString().length() == 1)
            Estante = "00" + Estante_Text.getText().toString();
        if (Estante_Text.getText().toString().length() == 2)
            Estante = "0" + Estante_Text.getText().toString();
        if (Estante_Text.getText().toString().length() == 3)
            Estante = Estante_Text.getText().toString();


        if (Integer.toString(HoraPlano.get(Calendar.HOUR_OF_DAY)).length() == 1)
            Hora = "0" + Integer.toString(HoraPlano.get(Calendar.HOUR_OF_DAY));
        if (Integer.toString(HoraPlano.get(Calendar.HOUR_OF_DAY)).length() == 2)
            Hora = Integer.toString(HoraPlano.get(Calendar.HOUR_OF_DAY));

        if (Integer.toString(HoraPlano.get(Calendar.MINUTE)).length() == 1)
            Minuto = "0" + Integer.toString(HoraPlano.get(Calendar.MINUTE));
        if (Integer.toString(HoraPlano.get(Calendar.MINUTE)).length() == 2)
            Minuto = Integer.toString(HoraPlano.get(Calendar.MINUTE));

        String HoraM = Hora + Minuto;
        int fecha = Integer.parseInt(FechaInv.getText().toString().replace("/", ""));
        int area = Integer.parseInt(Area);
        int estante = Integer.parseInt(Estante);
        int HoraContL = Integer.parseInt(HoraM);

        int read = db.buscarLectura(EANCODE, Estante);
        if (read > 0) {
            read = read + 1;
            lecturas = new Lecturas(fecha, Bodegas, area, Estante, 1, intCont,
                    EANCODE, UnMed, read, FechaInv.getText().toString(), HoraContL);
            db.updateLectura(lecturas);

        } else {
            lecturas = new Lecturas(fecha, Bodegas, area, Estante, 1, intCont,
                    EANCODE, UnMed, 1, FechaInv.getText().toString(), HoraContL);

            db.insertLectura(lecturas);
        }
    }

    public void DisplayAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confirmar");
        builder.setMessage("Hay datos por guardar, desea exportarlos?");

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExportDatabase();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDeleteDatabase();
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    public void AlertDeleteDatabase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Confrimar");
        builder.setMessage("Desea borrar la informacion?");

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteDatabase();
                ClearFormulario();
                DispDinamicAlert("Borrado", "Se ha borrado la informacion!");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void ExportDatabase() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
        Date now = new Date();
        String fileNamea = formatter.format(now);
        try {
            Calendar HoraAhora = Calendar.getInstance();
            int intNameFile = HoraAhora.get(Calendar.HOUR);

            if (BluetoohName == null) {
                BluetoohName = "No_define_name";
            }

            NameFile = BluetoohName + ConteoLetra + Integer.toString(HoraAhora.get
                    (Calendar.HOUR_OF_DAY)) +
                    Integer.toString(HoraAhora.get(Calendar.MINUTE)) + ".txt";

            root = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "Documents", "Conteos_" + fileNamea);

            if (!root.exists()) {
                root.mkdirs();
            }

            root.setExecutable(true);
            root.setReadable(true);
            root.setWritable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        gpxfile = new File(root, NameFile);
        if (db.ExportDataBase(gpxfile) == true) {
            DeleteDatabase();
            new loadFileInv().execute();
            DispDinamicAlert("Exportado", "Se han exportado los datos!!");
            DeleteDatabase();
        }
    }

    public void DeleteDatabase() {
        db.DeleteTableData();
    }

    public void DispDinamicAlert(String Titulo, String Mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(Titulo);
        builder.setMessage(Mensaje);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void AlertaBorrarUbicacion(final String[] Eanes, String Ubicacion, final String EanText){

        String ListaData = "";
        for (String Dato: Eanes) {
            ListaData = ListaData + Dato + "\r\n";
        }
        final String DeleteUbica =  ListaData, PosiNivel = Ubicacion;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confrimar");
        builder.setMessage("Esta ubicaion tiene lecturas desea:  Borrar o adicionar");
        VibraSuena();
        builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConfirmaBorrarUbicacion(DeleteUbica, PosiNivel, EanText, Eanes);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Adicionar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bodegas = Integer.parseInt(EanText.substring(0, 2));
                Area_Text.setText(EanText.substring(2, 4));
                Estante_Text.setText(EanText.substring(4, 7));
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


     public void ConfirmaBorrarUbicacion(String Datos, final String Ubicacion, final String EanText,
                                         final String[] CodEan){
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Confrimar");
         builder.setMessage("Desea borrar la informacion de esta ubicacion?: " + "\r\n" + Ubicacion +
                 "\r\n" + Datos);

         VibraSuena();

         builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 db.BorrarEstanteNivel(EanText.substring(5, 7), CodEan);
                 Bodegas = Integer.parseInt(EanText.substring(0, 2));
                 Area_Text.setText(EanText.substring(2, 4));
                 Estante_Text.setText(EanText.substring(4, 7));
                 DispDinamicAlert("Atencion","Se ha borrado la informacion");
                 dialog.dismiss();
             }
         });
         builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 DispDinamicAlert("Atencion","Se ha cambiado a la ubicacion: " + Ubicacion);
                 Bodegas = Integer.parseInt(EanText.substring(0, 2));
                 Area_Text.setText(EanText.substring(2, 4));
                 Estante_Text.setText(EanText.substring(4, 7));
                 dialog.dismiss();
             }
         });
         builder.create().show();
     }
     public void VibraSuena(){
         TonError.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 500);
         vibrator.vibrate(700);
     }
}
