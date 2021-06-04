package zgas.supervisor.models;

import com.google.android.gms.maps.model.LatLng;

import zgas.supervisor.tools.Point;
import zgas.supervisor.tools.Polygon;

public class Ruta {

    /*
     *   0 - ZetaGas
     *   1 - Thermogas ZI
     *   2 - Multigas
     *   3 - GasLicuado
     */


    Polygon cil24D;
    public Ruta()
    {
        constructorCilindros();


    }

    private void constructorCilindros() {
        cil24D = Polygon.Builder()
                .addVertex(new Point(-103.3334446,20.6033838))
                .addVertex(new Point(-103.3328384,20.6007124))
                .addVertex(new Point(-103.3292443,20.6008731))
                .addVertex(new Point(-103.3297753,20.6040165))
                .addVertex(new Point(-103.3334446,20.6033838))
                .build();
    }


    public String getRuta(LatLng latLng) {

        String ruta="";

        Point point = new Point(-103.3316204, 20.6022735);


        if(cil24D.contains(point))
            ruta = "24";

        return getLetra(latLng, ruta);
    }

    public String getLetra(LatLng latLng, String ruta)
    {
        String letra = "";

        return ruta + letra;
    }

    public int getEmpresa(LatLng latLng)
    {

        return 0;
    }










}
