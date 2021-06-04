package zgas.supervisor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import zgas.supervisor.includes.Popup;
import zgas.supervisor.includes.Toolbar;
import zgas.supervisor.models.Client;
import zgas.supervisor.providers.AuthProvider;
import zgas.supervisor.providers.ClientProvider;
import zgas.supervisor.tools.Validacion;

public class HomePerfil extends AppCompatActivity {

    private AuthProvider mAuthProvider;
    private ClientProvider mClientProvider;
    private EditText etNombre;
    private EditText etApellido;
    private Popup mPopup;
    private CircleImageView ivPerfil;
    boolean isNew;
    private CoordinatorLayout snackbar;
    private ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_perfil);

        Toolbar.show(this, true);
        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();
        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));
        pressUpdate = false;

        isNew = getIntent().getBooleanExtra("isNew", false);

        declaration();
        listenner();
    }

    private void load_data()
    {
    }

    private void listenner() {
        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen) {
                ivPerfil.setVisibility(View.GONE);
            } else {
                ivPerfil.setVisibility(View.VISIBLE);
            }
        });
    }

    private void declaration() {
        /// MASK 10 DIGITOS
        TextView tvTelefono = findViewById(R.id.tvTelefono);
        SimpleMaskFormatter smf1 = new SimpleMaskFormatter("+NN NN NNNN NNNN");
        MaskTextWatcher mtw1 = new MaskTextWatcher(tvTelefono, smf1);
        tvTelefono.addTextChangedListener(mtw1);
        tvTelefono.setText(mAuthProvider.getPhone());
        //

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        ivPerfil = findViewById(R.id.ivPerfil);

        if(!Home.mClient.getNombre().equals(""))
            etNombre.setText(Home.mClient.getNombre());
        if(!Home.mClient.getApellido().equals(""))
            etApellido.setText(Home.mClient.getApellido());

        snackbar = findViewById(R.id.snackbar_layout);
    }

    ///////////TOOLBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_check_ok, menu); //MOSTRAR
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.btnUpdate:
                UIUtil.hideKeyboard(HomePerfil.this); //ESCONDER TECLADO
                valDatos();
                break;

            case android.R.id.home:
                backPress();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    //

    private boolean pressUpdate;

    private void valDatos() {
        if(pressUpdate)
            return;
        else pressUpdate = true;

        Validacion mVal = new Validacion();

        if((etNombre.getText().toString()).equals("") || mVal.isOnlySpace(etNombre.getText().toString()))
        {
            //mPopup.setPopupError("El nombre es requerido.");
            Snackbar.make(snackbar, "El nombre es requerido.", Snackbar.LENGTH_LONG)
                    .setActionTextColor(getResources().getColor(R.color.white)).show();
            etNombre.requestFocus();
            SleepButton();
        }
        else if((etNombre.getText().toString()).trim().equals(Home.mClient.getNombre()) && (etApellido.getText().toString()).trim().equals(Home.mClient.getApellido())){
            finish();
        }
        else if(!isConnected())
        {
            runOnUiThread(() -> {
                Snackbar snackbar1;
                snackbar1 = Snackbar.make(snackbar, "No hay conexión a internet, no puedes actualizar tus datos personales.", Snackbar.LENGTH_SHORT);
                View snackBarView = snackbar1.getView();
                snackBarView.setBackgroundColor(getResources().getColor(R.color.rojoAlert));
                snackbar1.show();
            });
            SleepButton();
        }
        else
        {
            Home.mClient = new Client(mAuthProvider.getId(), etNombre.getText().toString().trim(), etApellido.getText().toString().trim(), mAuthProvider.getPhone());
            mClientProvider.update(Home.mClient).addOnCompleteListener(taskCreate -> {
                if (taskCreate.isSuccessful()) {
                    if(isNew)
                    {
                        Client.setIsLoad(true);
                        load_data();
                        Intent intent = new Intent(HomePerfil.this, Home.class);
                        startActivity(intent);

                        guardar_datos();
                    }
                    else
                    {
                        runOnUiThread(() -> {
                            Home.tvNombre.setText(Home.mClient.getNombre());
                            Home.tvApellido.setText(Home.mClient.getApellido());
                        });
                        guardar_datos();

                        finish();
                    }

                }
                else {
                    mPopup.setPopupError("No se pudo registrar los datos, intentalo más tarde.");
                    pressUpdate = false;
                }
            });
        }
    }

    private boolean isConnected(){
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void guardar_datos()
    {

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
            @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            FileOutputStream conf = openFileOutput("Acc_App", Context.MODE_PRIVATE);
            String cadena =
                    dateFormat.format(Data) + "\n" +
                            Home.mClient.getNombre() + "\n" +
                            Home.mClient.getApellido() + "\n";

            conf.write(cadena.getBytes());
            conf.close();
        }
        catch(Exception ignored){}
    }


    private void SleepButton()
    {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pressUpdate = false;
        }).start();
    }

    //BACK PRESS

    @Override
    public void onBackPressed() {
        backPress();
    }

    //DEP
    private void backPress() {
        if(isNew)
            this.moveTaskToBack(true); //Minimizar
        else
            finish();
    }
    //BACK PRESS
}