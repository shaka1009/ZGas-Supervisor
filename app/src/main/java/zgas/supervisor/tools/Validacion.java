package zgas.supervisor.tools;

public class Validacion {

    public boolean isOnlySpace(String cad)
    {
        for(int i =0; i<cad.length(); i++)
            if(cad.charAt(i) != ' ')
                return false;

        return true;
    }
}
