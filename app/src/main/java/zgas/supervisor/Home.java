package zgas.supervisor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.StringTokenizer;

import zgas.supervisor.includes.Popup;
import zgas.supervisor.includes.Toolbar;
import zgas.supervisor.models.Client;
import zgas.supervisor.providers.AuthProvider;
import zgas.supervisor.providers.ClientProvider;
import zgas.supervisor.providers.TokenProvider;

public class Home extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static Activity activity;



    private AuthProvider mAuthProvider;
    ClientProvider mClientProvider;
    TokenProvider mTokenProvider;

    public static Client mClient;
    @SuppressLint("StaticFieldLeak")
    public static TextView tvTelefono, tvNombre, tvApellido;


    private DrawerLayout drawer;
    private CoordinatorLayout snackbar;
    private Popup mPopup;



    private Button btnPedir;
    private Button btnPerfil;
    private Button btnDomicilios;
    private Button btnRegalos;
    private Button btnPedidos;
    private ConnectivityManager cm;

    private boolean pressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar.showHome(this, true);
        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));

        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();
        mTokenProvider = new TokenProvider();

        activity = this;

        drawerMain();
        declaration();
        listenner();
        generateToken();

        load_first_day();
        close_activitys();
    }

    ////////////CARGA DE DATOS
    private void load_first_day() {
        //Load Perfil
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                load_datos();
            }
        }).start();
    }

    private void close_activitys() {
        new Thread(() -> {
            while(true)
            {
                if(Client.isLoad())
                {
                    try {
                        MainActivity.activity.finish();
                    }catch (Exception ignored){}

                    try {
                        LoginSMS.activity.finish();
                    }catch (Exception ignored){}

                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("DEP", "Cargando datos");
                    }
                });
            }

        }).start();
    }

    private void load_datos()
    {
        try {
            FileInputStream read = openFileInput("Acc_App");
            int size = read.available();
            byte[] buffer = new byte[size];
            read.read(buffer);
            read.close();
            String text = new String(buffer);
            StringTokenizer token = new StringTokenizer(text, "\n");
            String fecha = token.nextToken();

            java.util.Date Data = new Date();
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            if(fecha.equals(dateFormat.format(Data)))
            {

                Home.mClient.setTelefono(token.nextToken());
                Home.mClient.setNombre(token.nextToken());
                Home.mClient.setApellido(token.nextToken());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvTelefono.setText(mClient.getTelefono());
                        tvNombre.setText(mClient.getNombre());
                        tvApellido.setText(mClient.getApellido());
                    }
                });
                Client.setIsLoad(true);
            }
            else
                guardar_datos();
        }
        catch(Exception e){
            guardar_datos();
        }

    }

    private void declaration() {
        btnPedir = findViewById(R.id.btnPedir);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnDomicilios = findViewById(R.id.btnDomicilios);
        btnRegalos = findViewById(R.id.btnRegalos);
        btnPedidos = findViewById(R.id.btnPedidos);

        snackbar = findViewById(R.id.snackbar_layout);
    }

    private void guardar_datos()
    {
        new Thread(() -> mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String id = mAuthProvider.getId();
                    String nombre = Objects.requireNonNull(snapshot.child("nombre").getValue()).toString();
                    String apellido = Objects.requireNonNull(snapshot.child("apellido").getValue()).toString();
                    String telefono = mAuthProvider.getPhone();
                    Home.mClient = new Client(id, nombre, apellido, telefono);
                    Client.setIsLoad(true);

                    Log.d("DEP", "Datos en variable.");

                    try{
                        //DELETE FILE
                        try{
                            try{
                                deleteFile("Acc_App");
                            }catch(Exception ignored){}

                            File f = new File("Acc_App");
                            f.delete();
                        }catch(Exception ignored){}
                        //

                        java.util.Date Data = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                        FileOutputStream conf = openFileOutput("Acc_App", Context.MODE_PRIVATE);
                        String cadena =
                                dateFormat.format(Data) + "\n" +
                                        Home.mClient.getTelefono() + "\n" +
                                        Home.mClient.getNombre() + "\n" +
                                        Home.mClient.getApellido() + "\n";

                        conf.write(cadena.getBytes());
                        conf.close();
                    }
                    catch(Exception ignored){}

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvTelefono.setText(mClient.getTelefono());
                            tvNombre.setText(mClient.getNombre());
                            tvApellido.setText(mClient.getApellido());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        })).start();
    }

    void generateToken() {
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                while(true)
                {
                    if(isConnected())
                    {
                        mTokenProvider.create(mAuthProvider.getId());
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private boolean isConnected(){
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @SuppressLint("NonConstantResourceId")
    private void drawerMain()
    {
        //drawer.openDrawer(Gravity.LEFT);
        //drawer.closeDrawer(Gravity.LEFT);


        //drawer
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);

        tvTelefono = hView.findViewById(R.id.slide_telefono);
        tvNombre = hView.findViewById(R.id.slide_nombre);
        tvApellido = hView.findViewById(R.id.slide_apellido);
        //drawer

        /// MASK 10 DIGITOS
        SimpleMaskFormatter smf1 = new SimpleMaskFormatter("+NN NN NNNN NNNN");
        MaskTextWatcher mtw1 = new MaskTextWatcher(tvTelefono, smf1);
        tvTelefono.addTextChangedListener(mtw1);





        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId())
            {
                case R.id.menu_perfil:
                    if(pressButton)
                        break;
                    else pressButton = true;

                    if(!Client.isLoad())
                    {
                        View v=findViewById(R.id.menu_perfil);
                        Snackbar.make(v, "No se han cargado los datos necesarios.", Snackbar.LENGTH_LONG)
                                .setActionTextColor(getResources().getColor(R.color.white)).show();
                        pressButton = false;
                        break;
                    }
                    Intent b = new Intent(Home.this, HomePerfil.class );
                    startActivity(b);
                    SleepButton();
                    break;

                case R.id.cerrar_sesion:
                    if(pressButton)
                        break;
                    else pressButton = true;

                    try
                    {
                        mPopup.setPopupCerrarSesion(mClient.getNombre(), mClient.getApellido());
                    }
                    catch (Exception ignored){}

                    SleepButton();
                    break;

                case R.id.acerca_de_app:
                    if(pressButton)
                        break;
                    else pressButton = true;

                    //Intent intent = new Intent(Home.this, HomeAcerca.class);
                    //startActivity(intent);

                    SleepButton();
                    break;




                case R.id.menu_sucursales:
                    if(pressButton)
                        break;
                    else pressButton = true;

                    //Intent sucursales = new Intent(this, HomeSucursales.class);
                    //startActivity(sucursales);

                    SleepButton();
                    break;



            }
            drawer.closeDrawers();
            return false;
        });
    }

    private void SleepButton()
    {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pressButton = false;
        }).start();
    }



    private void listenner() {
        pressButton = false;

        mPopup.popupCerrarSesionSalir.setOnClickListener(v -> new Thread(() -> {
            mPopup.hidePopupCerrarSesion();
            if(pressButton)
                return;
            else pressButton = true;
            try{
                try{
                    deleteFile("Acc_App");
                }catch(Exception ignored){}

                File f = new File("Acc_App");
                f.delete();
            }catch(Exception ignored){}


            mAuthProvider.logout();
            Intent intent = new Intent(Home.this, Login.class);
            startActivity(intent);
            SleepButton();
            finish();
        }).start());

        mPopup.btnPoupAddAccAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pressButton)
                    return;
                else pressButton = true;
                //Intent register = new Intent(Home.this , HomeDirecciones.class);
                //startActivity(register);
                SleepButton();
                mPopup.hidePopupAddAcc();
            }
        });

        btnPedir.setOnClickListener(v -> {
            if(pressButton)
                return;
            else pressButton = true;

            if(!Client.isLoad())
            {
                Snackbar.make(v, "No se han cargado los datos necesarios.", Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.white)).show();
                pressButton = false;
                return;
            }

            Intent intent = new Intent(Home.this , HomeVisor.class);
            startActivity(intent);
            SleepButton();
        });

        mPopup.btnPoupAddAccAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(Home.this , HomeDirecciones.class);
                //startActivity(intent);
                mPopup.hidePopupAddAcc();
                SleepButton();
            }
        });

        btnPerfil.setOnClickListener(v -> {
            if(pressButton)
                return;
            else pressButton = true;

            if(!Client.isLoad())
            {
                Snackbar.make(v, "No se han cargado los datos necesarios.", Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.white)).show();
                pressButton = false;
                return;
            }

            Intent register = new Intent(Home.this , HomeRegistrarOperador.class);
            startActivity(register);
            SleepButton();
        });

        btnDomicilios.setOnClickListener(v -> {
            if(pressButton)
                return;
            else pressButton = true;


            /*
            if(!Direcciones.isLoad())
            {
                Snackbar.make(v, "No se han cargado los datos necesarios.", Snackbar.LENGTH_LONG)
                        .setActionTextColor(getResources().getColor(R.color.white)).show();
                pressButton = false;
                return;
            }
            */


            Intent HomeDomicilios = new Intent(Home.this , HomeOperadores.class);
            startActivity(HomeDomicilios);


            SleepButton();
        });

        btnRegalos.setOnClickListener(v -> {
            if(pressButton)
                return;
            else pressButton = true;
            //Intent register = new Intent(Home.this , Home_regalos.class);
            //startActivity(register);
            SleepButton();
        });

        btnPedidos.setOnClickListener(v -> {
            if(pressButton)
                return;
            else pressButton = true;
            //Intent register = new Intent(Home.this , HomeHistorial.class);
            //startActivity(register);
            SleepButton();
        });
    }


    @SuppressLint("RtlHardcoded")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            drawer.openDrawer(Gravity.LEFT);
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true); //Minimizar
    }
}