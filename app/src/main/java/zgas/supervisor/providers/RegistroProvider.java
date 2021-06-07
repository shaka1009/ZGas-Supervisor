package zgas.supervisor.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


import zgas.supervisor.models.Driver;
import zgas.supervisor.models.IAData;
import zgas.supervisor.models.Registro;

public class RegistroProvider {

    DatabaseReference mDatabase;

    public RegistroProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Registro").child("Operadores");
    }


    public DatabaseReference getIA(String numNomina) {
        return mDatabase.child(numNomina);
    }


    public Task<Void> create(Registro registro) {
        Map<String, Object> map = new HashMap<>();
        map.put("finish", 0);
        map.put("telefono", registro.getTelefono());
        map.put("nombre", registro.getNombre());
        map.put("apellido", registro.getApellido());
        return mDatabase.child(String.valueOf(registro.getNumNomina())).updateChildren(map);
    }

    public Task<Void> update(Registro registro) {
        Map<String, Object> map = new HashMap<>();
        map.put("finish", 1);
        return mDatabase.child(String.valueOf(registro.getNumNomina())).updateChildren(map);
    }


    public Task<Void>  createIA(String numNomina, IAData iaData) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", iaData.getId());
        map.put("title", iaData.getTitle());
        map.put("distance", iaData.getDistance());
        map.put("left", iaData.getLeft());
        map.put("top", iaData.getTop());
        map.put("right", iaData.getRight());
        map.put("bottom", iaData.getBottom());
        map.put("color", iaData.getColor());

        return mDatabase.child(numNomina).child("IAData").setValue(map);

    }

    public DatabaseReference getIAt(String numNomina) {
        return mDatabase.child(numNomina).child("IAData");
    }




    public DatabaseReference getOperador(String numNomina) {
        return mDatabase.child(numNomina);
    }
}
