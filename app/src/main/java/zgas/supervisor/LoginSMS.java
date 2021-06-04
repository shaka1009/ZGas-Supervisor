package zgas.supervisor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
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
import java.util.Locale;

import zgas.supervisor.includes.Popup;
import zgas.supervisor.includes.Toolbar;
import zgas.supervisor.models.Client;
import zgas.supervisor.providers.AuthProvider;
import zgas.supervisor.providers.ClientProvider;

public class LoginSMS extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Activity activity;

    private AuthProvider mAuthProvider;
    private ClientProvider mClientProvider;
    private ConnectivityManager cm;
    private Popup mPopup;

    private EditText etCodigo;
    private TextView tvTelefono, tvMsgContinuar, tvInfo;
    private ImageView logo_zeta;
    private Button btnCheck, btnReSend;
    private ProgressBar pbLoginSMS;

    private String telefono;
    private static String Credentials = "";


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sms);

        setContentView(R.layout.activity_login_sms);
        Toolbar.show(this, true);
        cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        activity = this;
        telefono = getIntent().getStringExtra("telefono");

        declaration();
        listenner();
        callBacks();


        if(Credentials.equals(""))
            loadingVisible(true);

        new Thread(new Runnable(){
            @Override
            public void run()
            {
                while(!isConnected())
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mAuthProvider.sendCodeVerification(LoginSMS.this, telefono, mCallBacks);
            }
        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void callBacks() {
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                tvInfo.setText("Error al intentar mandar el SMS, intentalo más tarde.");
                loadingVisible(false);
            }
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Credentials  = s;
                loadingVisible(false);
                contador();
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                loadingVisible(false);
                //reenviar
                btnReSend.setVisibility(View.VISIBLE);
                Credentials = "";
                tvInfo.setText("El tiempo de espera ha terminado, intenta de nuevo.");
            }
        };
    }

    private boolean isConnected(){
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void contador() {
        new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvInfo.setText(String.format(Locale.getDefault(), "Reenviar código en %d seg.", millisUntilFinished / 1000L));
            }

            public void onFinish() {
            }
        }.start();
    }


    @SuppressLint("SetTextI18n")
    private void declaration() {

        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();

        //Popup - Error
        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));

        /// MASK 6 DIGITOS
        etCodigo = findViewById(R.id.etCodigo);
        SimpleMaskFormatter smf = new SimpleMaskFormatter("N N N N N N");
        MaskTextWatcher mtw = new MaskTextWatcher(etCodigo, smf);
        etCodigo.addTextChangedListener(mtw);
        //

        /// MASK 10 DIGITOS
        tvTelefono = findViewById(R.id.tvTelefono);
        SimpleMaskFormatter smf1 = new SimpleMaskFormatter("+NN NN NNNN NNNN");
        MaskTextWatcher mtw1 = new MaskTextWatcher(tvTelefono, smf1);
        tvTelefono.addTextChangedListener(mtw1);
        tvTelefono.setText("+52" + telefono);
        //

        btnCheck = findViewById(R.id.btnCheck);
        btnReSend = findViewById(R.id.btnReSend);
        logo_zeta = findViewById(R.id.logo_zeta);
        tvMsgContinuar = findViewById(R.id.tvMsgContinuar);
        pbLoginSMS = findViewById(R.id.pbLoginSMS);

        tvInfo  = findViewById(R.id.tvInfo);
    }

    boolean btnCheckPress = false;
    private void listenner() {
        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen) {
                logo_zeta.setVisibility(View.GONE);
                tvMsgContinuar.setVisibility(View.GONE);
                tvTelefono.setVisibility(View.GONE);
                //LinearDots.setVisibility(View.INVISIBLE);
            } else {
                logo_zeta.setVisibility(View.VISIBLE);
                tvMsgContinuar.setVisibility(View.VISIBLE);
                tvTelefono.setVisibility(View.VISIBLE);
                //LinearDots.setVisibility(View.VISIBLE);
            }
        });

        btnReSend.setOnClickListener(v -> {
            if(btnCheckPress)
                return;
            else btnCheckPress = true;
            tvInfo.setText("");
            loadingVisible(true);
            mAuthProvider.sendCodeVerification(LoginSMS.this, telefono, mCallBacks);
            btnReSend.setVisibility(View.INVISIBLE);
            btnCheckPress = false;
        });

        btnCheck.setOnClickListener(v -> {
            if(btnCheckPress)
                return;
            else btnCheckPress = true;

            runOnUiThread(() -> loadingVisible(true));

            new Thread(() -> {
                UIUtil.hideKeyboard(LoginSMS.this); //ESCONDER TECLADO

                if (etCodigo.length() == 0)
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("Ingresa el código que se te envió.");
                        btnCheckPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else if (etCodigo.length() != 11)
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("Debes ingresar los 6 dígitos.");
                        btnCheckPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else
                {
                    new Thread(() -> {
                        if(Credentials.equals(""))
                        {
                            runOnUiThread(() -> {
                                etCodigo.setText("");
                                mPopup.setPopupError("Has ingresado un código incorrecto o ya caducado.");
                                btnCheckPress = false;
                                runOnUiThread(() -> loadingVisible(false));
                            });
                        }
                        else
                        {
                            String verification_code = etCodigo.getText().toString().replaceAll(" ", "");
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(Credentials , verification_code);
                            signIn(credential);
                        }

                    }).start();
                }
            }).start();
        });

    }

    private void signIn(PhoneAuthCredential credential){
        mAuthProvider.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            String id = mAuthProvider.getId();
                            String nombre = snapshot.child("nombre").getValue().toString();
                            String apellido = snapshot.child("apellido").getValue().toString();
                            String telefono = mAuthProvider.getPhone();
                            Home.mClient = new Client(id, nombre, apellido, telefono);

                            guardar_datos();
                            Client.setIsLoad(true);

                            load_data();

                            Intent intent = new Intent(LoginSMS.this, Home.class);
                            startActivity(intent);

                            if(Client.isLoad())
                            {
                                Credentials="";
                                finish();
                            }
                        }
                        else {
                            Intent intent = new Intent(LoginSMS.this, HomePerfil.class);
                            intent.putExtra("isNew", true);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });



            }else{
                mPopup.setPopupError("Has ingresado un código incorrecto o ya caducado.");
                btnCheckPress = false;
                loadingVisible(false);
                etCodigo.setText("");
                etCodigo.requestFocus();
            }
        });
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
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

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

    private void load_data()
    {

    }

    private void loadingVisible(boolean b)
    {
        if(b)
        {
            btnCheck.setVisibility(View.INVISIBLE);
            pbLoginSMS.setVisibility(View.VISIBLE);
            etCodigo.setEnabled(false);
        }
        else
        {
            btnCheck.setVisibility(View.VISIBLE);
            pbLoginSMS.setVisibility(View.INVISIBLE);
            etCodigo.setEnabled(true);
        }
    }


    //BACK PRESS
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            backPress();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    //DEP
    private void backPress() {
        finish();
    }
    //BACK PRESS
}