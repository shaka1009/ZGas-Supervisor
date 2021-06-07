package zgas.supervisor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import zgas.supervisor.IA.DetectorActivity;
import zgas.supervisor.models.Client;
import zgas.supervisor.models.IAData;
import zgas.supervisor.providers.AuthProvider;
import zgas.supervisor.providers.RegistroProvider;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Activity activity;

    private AuthProvider mAuthProvider;


    int dep = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme_Splash);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        mAuthProvider = new AuthProvider();






        Home.mClient = new Client();
        /*
        Home.mPrice = new Price();
        Home.mDirecciones = new ArrayList<>();
        */

    }

    @Override
    protected void onStart() {
        super.onStart();
/*
        Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
        startActivity(intent);
*/

        if(mAuthProvider.existSession())
        {
            //load_data();


            //Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
            //startActivity(intent);


            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);



            //cargarDatosFB("73539");


        }
        ///* EN PRUEBA, NO QUITAR
        else
        {
            if(dep==1)
                return;
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        }
        //





    }







    @Override
    public void onBackPressed() {}
}