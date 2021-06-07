package zgas.supervisor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import zgas.supervisor.IA.DetectorActivity;
import zgas.supervisor.includes.Popup;
import zgas.supervisor.includes.Toolbar;
import zgas.supervisor.models.Client;
import zgas.supervisor.models.Registro;
import zgas.supervisor.providers.AuthProvider;
import zgas.supervisor.providers.RegistroProvider;

public class HomeRegistrarOperador extends AppCompatActivity {

    EditText etNomina, etTelefono, etNombre, etApellido;

    FloatingActionButton btnFotoPerfil;

    CircleImageView cvFoto1;

    private Popup mPopup;

    CheckBox checkBoxIA;
    Button button2;
    LinearLayout LinearLayoutIA;




    Registro registro;
    RegistroProvider registroProvider;

    AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_registrar_operador);
        Toolbar.show(this, true);
        mPopup = new Popup(this, getApplicationContext(), findViewById(R.id.popupError));
        registro = new Registro();
        registroProvider = new RegistroProvider();
        mAuthProvider = new AuthProvider();

        declaration();
        listenner();

    }

    private void listenner() {
        btnFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara(1);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valNomina();
            }
        });
    }

    private void valNomina() {



        try {

            registro.setNumNomina(etNomina.getText().toString());


            if((Integer.parseInt(registro.getNumNomina()))   ==0)
            {
                Toast.makeText(this, "Error en número de nómina, no puede ser 0.", Toast.LENGTH_SHORT).show();
            }


            //Validar en DB
            try {
                registroProvider.getOperador(String.valueOf(registro.getNumNomina())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                if(Integer.parseInt(Objects.requireNonNull(snapshot.child("finish").getValue()).toString()) == 1)
                                    Toast.makeText(HomeRegistrarOperador.this, "Ya existe un número de nómina.", Toast.LENGTH_SHORT).show();
                                else
                                {
                                    siguiente();
                                }

                            }
                            catch (Exception e)
                            {
                                siguiente();
                            }


                        }
                        else
                        {
                            siguiente();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }catch (Exception e) { }
            //
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Error en número de nómina: no has ingresado un número", Toast.LENGTH_SHORT).show();
        }
    }

    private void siguiente()
    {
        Intent intent = new Intent(HomeRegistrarOperador.this, DetectorActivity.class);
        intent.putExtra("numNomina", etNomina.getText().toString());
        startActivityForResult(intent, 2);
    }

    private void declaration()
    {
        etNomina = findViewById(R.id.etNomina);

        /// MASK 10 DIGITOS
        etTelefono = findViewById(R.id.etTelefono);
        SimpleMaskFormatter smf = new SimpleMaskFormatter("NN NNNN NNNN");
        MaskTextWatcher mtw = new MaskTextWatcher(etTelefono, smf);
        etTelefono.addTextChangedListener(mtw);
        etTelefono.requestFocus();
        //

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);

        btnFotoPerfil = findViewById(R.id.btnFotoPerfil);

        cvFoto1 = findViewById(R.id.ivPerfil);

        checkBoxIA = findViewById(R.id.checkBox);
        LinearLayoutIA = findViewById(R.id.LinearLayoutIA);
        button2 = findViewById(R.id.button2);

        etNomina.requestFocus();
    }

    private void abrirCamara(int code){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, code);
        }
    }

    boolean valFoto1 = false, valFoto2 = false, valFoto3 = false;

    Bitmap imgBitmap1;
    Bitmap imgBitmap2;
    Bitmap imgBitmap3;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imgBitmap1 = (Bitmap) extras.get("data");
            cvFoto1.setImageBitmap(imgBitmap1);
            valFoto1 = true;
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            checkBoxIA.setChecked(true);
        }
    }

    private boolean valFoto()
    {
        if(valFoto1)
            return true;
        else
            return false;
    }

    private void valDatos()
    {

        if(!valFoto())
            Toast.makeText(this, "Necesitas tomar foto de perfil.", Toast.LENGTH_SHORT).show();
        else
        {
            try {

                registro.setNumNomina(etNomina.getText().toString());


                if((Integer.parseInt(registro.getNumNomina()))   ==0)
                {
                    Toast.makeText(this, "Error en número de nómina, no puede ser 0.", Toast.LENGTH_SHORT).show();
                }


                //Validar en DB
                try {
                    registroProvider.getOperador(String.valueOf(registro.getNumNomina())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                try {
                                    if(Integer.parseInt(Objects.requireNonNull(snapshot.child("finish").getValue()).toString()) == 1)
                                        Toast.makeText(HomeRegistrarOperador.this, "Ya existe un número de nómina.", Toast.LENGTH_SHORT).show();
                                    else
                                        valTelefono(registro.getNumNomina());
                                }
                                catch (Exception e)
                                {
                                    valTelefono(registro.getNumNomina());
                                }

                            }
                            else
                            {
                                valTelefono(registro.getNumNomina());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }catch (Exception e) { }
                //


            }

            catch (Exception e)
            {
                Toast.makeText(this, "Error en número de nómina: no has ingresado un número", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void valTelefono(String numNomina)
    {
        registro.setTelefono(etTelefono.getText().toString().replaceAll(" ", ""));

        if (etTelefono.length() == 0)
        {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ingresa un número de teléfono.", Toast.LENGTH_SHORT).show();
            });
        }
        else if (etTelefono.length() != 12)
        {
            runOnUiThread(() -> {
                Toast.makeText(this, "El número debe contener 10 dígitos.", Toast.LENGTH_SHORT).show();
            });
        }
        else
        {
            valNombreApellido(numNomina);
        }
    }

    private void valNombreApellido(String numNomina) {

        registro.setNombre(etNombre.getText().toString());
        registro.setApellido(etApellido.getText().toString());


        if (etNombre.length() == 0)
        {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ingresa un nombre de operador.", Toast.LENGTH_SHORT).show();
            });
        }
        else if (etApellido.length() == 0)
        {
            runOnUiThread(() -> {
                Toast.makeText(this, "Ingresa los apellidos del operador.", Toast.LENGTH_SHORT).show();
            });
        }
        else
        {

            registroProvider.create(registro).addOnCompleteListener(taskCreate -> {
                if (taskCreate.isSuccessful()) {
                    subirFotos(numNomina);
                }
                else {
                    Toast.makeText(HomeRegistrarOperador.this, "Error en el registro.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void subirFotos(String numNomina) {






        int quality=100;
        int width = 2160;
        int height = 2640;
        if(valFoto())
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference foto1 = storage.getReference().child(registro.getNumNomina() + "Perfil.jpg");

            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();

            imgBitmap1 = Bitmap.createScaledBitmap(imgBitmap1, width, height, false);
            imgBitmap1.compress(Bitmap.CompressFormat.JPEG, quality, baos1);
            byte[] data1 = baos1.toByteArray();

            UploadTask uploadTask = foto1.putBytes(data1);
            uploadTask.addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    registroProvider.update(registro).addOnCompleteListener(taskCreate -> {
                        if (taskCreate.isSuccessful()) {
                            Toast.makeText(HomeRegistrarOperador.this, "Registro exitoso.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else {
                            Toast.makeText(HomeRegistrarOperador.this, "Error en el registro.", Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });
        }
    }


    //BACK PRESS

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
                UIUtil.hideKeyboard(HomeRegistrarOperador.this); //ESCONDER TECLADO
                valDatos();
                break;

            case android.R.id.home:
                backPress();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    //



    @Override
    public void onBackPressed() {
        backPress();
    }

    private void backPress() {
        finish();
    }
}