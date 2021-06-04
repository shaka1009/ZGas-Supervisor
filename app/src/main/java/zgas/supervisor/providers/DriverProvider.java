package zgas.supervisor.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import zgas.supervisor.models.Driver;

public class DriverProvider {

    DatabaseReference mDatabase;

    public DriverProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Usuarios").child("Operadores");
    }

    public Task<Void> create(Driver driver) {
        return mDatabase.child(driver.getId()).child("Datos").setValue(driver);
    }

    public DatabaseReference getDriver(String idDriver) {
        return mDatabase.child(idDriver).child("Datos");
    }

    public Task<Void> update(Driver driver) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", driver.getNombre());
        map.put("apellido", driver.getApellido());
        return mDatabase.child(driver.getId()).child("Datos").updateChildren(map);
    }

}
