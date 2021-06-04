package zgas.supervisor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import zgas.supervisor.includes.Popup;

public class Login extends AppCompatActivity {

    private EditText etTelefono;
    Button btnSend;
    private Popup mPopup;
    private ProgressBar pbLogin;

    String telefono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        declaration();
        listenner();
    }

    private void declaration() {
        /// MASK 10 DIGITOS
        etTelefono = findViewById(R.id.etTelefono);
        SimpleMaskFormatter smf = new SimpleMaskFormatter("NN NNNN NNNN");
        MaskTextWatcher mtw = new MaskTextWatcher(etTelefono, smf);
        etTelefono.addTextChangedListener(mtw);
        etTelefono.requestFocus();
        //

        //Popup - Error
        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));

        //Button Send
        btnSend = findViewById(R.id.btnSend);

        //Progress - bar
        pbLogin = findViewById(R.id.pbLogin);
    }
    boolean btnSendPress = false;

    private void listenner()
    {
        //Esconder Teclado
        LinearLayout titleLogin = findViewById(R.id.titleLogin);
        KeyboardVisibilityEvent.setEventListener(this, isOpen -> {
            if (isOpen) {
                titleLogin.setVisibility(View.GONE);
                //LinearDots.setVisibility(View.GONE);
            } else {
                titleLogin.setVisibility(View.VISIBLE);
                //LinearDots.setVisibility(View.VISIBLE);
            }
        });

        loadingVisible(false);

        btnSend.setOnClickListener(v -> {
            if(btnSendPress)
                return;
            else btnSendPress = true;

            runOnUiThread(() -> loadingVisible(true));

            telefono = etTelefono.getText().toString().replaceAll(" ", "");

            new Thread(() -> {
                UIUtil.hideKeyboard(Login.this); //ESCONDER TECLADO

                if (etTelefono.length() == 0)
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("Ingresa un número de teléfono.");
                        btnSendPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else if (etTelefono.length() != 12)
                {
                    runOnUiThread(() -> {
                        mPopup.setPopupError("El número debe contener 10 dígitos.");
                        btnSendPress = false;
                        runOnUiThread(() -> loadingVisible(false));
                    });
                }
                else
                {
                    new Thread(() -> {
                        Intent otpIntent = new Intent(Login.this , LoginSMS.class);
                        otpIntent.putExtra("telefono", telefono);
                        startActivity(otpIntent);
                        finish();
                    }).start();
                }
            }).start();
        });
    }

    private void loadingVisible(boolean b)
    {
        if(b)
        {
            btnSend.setVisibility(View.INVISIBLE);
            pbLogin.setVisibility(View.VISIBLE);
            etTelefono.setEnabled(false);
        }
        else
        {
            btnSend.setVisibility(View.VISIBLE);
            pbLogin.setVisibility(View.INVISIBLE);
            etTelefono.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}