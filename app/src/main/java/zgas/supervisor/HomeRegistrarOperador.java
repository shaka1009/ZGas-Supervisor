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
import android.widget.EditText;
import android.widget.ImageView;
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
import zgas.supervisor.includes.Popup;
import zgas.supervisor.includes.Toolbar;
import zgas.supervisor.models.Client;
import zgas.supervisor.models.Registro;
import zgas.supervisor.providers.AuthProvider;
import zgas.supervisor.providers.RegistroProvider;

public class HomeRegistrarOperador extends AppCompatActivity {

    EditText etNomina, etTelefono, etNombre, etApellido;

    ImageView imvFoto1, imvFoto2, imvFoto3;

    FloatingActionButton btnFotoPerfil;

    CircleImageView cvFoto1;

    private Popup mPopup;



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


        imvFoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara(1);
            }
        });

        imvFoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara(2);
            }
        });

        imvFoto3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara(3);
            }
        });


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

        imvFoto1 = findViewById(R.id.imvFoto1);
        imvFoto2 = findViewById(R.id.imvFoto2);
        imvFoto3 = findViewById(R.id.imvFoto3);

        cvFoto1 = findViewById(R.id.ivPerfil);
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
            imvFoto1.setImageBitmap(imgBitmap1);
            cvFoto1.setImageBitmap(imgBitmap1);
            valFoto1 = true;

        }
        else if (requestCode == 2 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imgBitmap2 = (Bitmap) extras.get("data");
            imvFoto2.setImageBitmap(imgBitmap2);
            valFoto2 = true;
        }
        else if (requestCode == 3 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imgBitmap3 = (Bitmap) extras.get("data");
            imvFoto3.setImageBitmap(imgBitmap3);
            valFoto3 = true;
        }
    }

    private boolean valFoto()
    {
        if(valFoto1 && valFoto2 && valFoto3)
            return true;
        else
            return false;
    }





    private void valDatos()
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
                            if(Integer.parseInt(Objects.requireNonNull(snapshot.child("finish").getValue()).toString()) == 1)
                                Toast.makeText(HomeRegistrarOperador.this, "Ya existe un número de nómina.", Toast.LENGTH_SHORT).show();
                            else
                                valTelefono();

                        }
                        else
                        {
                            valTelefono();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

            }catch (Exception e)
            {

            }
            //


        }

        catch (Exception e)
        {
            Toast.makeText(this, "Error en número de nómina: no has ingresado un número", Toast.LENGTH_SHORT).show();
        }


    }

    private void valTelefono()
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
            valNombreApellido();
        }







    }

    private void valNombreApellido() {

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
                    subirFotos();
                }
                else {
                    Toast.makeText(HomeRegistrarOperador.this, "Error en el registro.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void subirFotos() {

        int quality=100;
        int width = 2160;
        int height = 2640;
        if(valFoto())
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference foto1 = storage.getReference().child(registro.getNumNomina()).child("Foto1.jpg");
            StorageReference foto2 = storage.getReference().child(registro.getNumNomina()).child("Foto2.jpg");
            StorageReference foto3 = storage.getReference().child(registro.getNumNomina()).child("Foto3.jpg");

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
                    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    imgBitmap2 = Bitmap.createScaledBitmap(imgBitmap2, width, height, false);
                    imgBitmap2.compress(Bitmap.CompressFormat.JPEG, quality, baos2);
                    byte[] data2 = baos2.toByteArray();

                    UploadTask uploadTask2 = foto2.putBytes(data2);
                    uploadTask2.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
                            imgBitmap3 = Bitmap.createScaledBitmap(imgBitmap3, width, height, false);
                            imgBitmap3.compress(Bitmap.CompressFormat.JPEG, quality, baos3);
                            byte[] data3 = baos3.toByteArray();

                            UploadTask uploadTask3 = foto3.putBytes(data3);
                            uploadTask3.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
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