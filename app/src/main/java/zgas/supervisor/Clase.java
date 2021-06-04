package zgas.supervisor;

import java.io.Serializable;

public class Clase implements Serializable {
	String nombre;

	public Clase (String gato)

	{	
		nombre = gato;
	}

	
	public String getGato()
	{
		return nombre;
	}
}
