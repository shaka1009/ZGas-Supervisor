package zgas.supervisor.includes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import zgas.supervisor.R;

public class Popup {
    Context context;
    Context contextApp;
    View error;
    boolean pressButton;

    //POPUP ERROR
    private BottomSheetDialog popupError;
    public Button btnPoupErrorAceptar;
    //private TextView tvPoupError;
    //POPUP ADD ACC
    private BottomSheetDialog popupAddAcc;
    public Button btnPoupAddAccAceptar;
    //private TextView tvPoupAddAcc;
    //POPUP CERRAR SESIÓN
    private Dialog popupCerrarSesion;
    public Button popupCerrarSesionCancelar, popupCerrarSesionSalir;
    private TextView tvPopupCerrarSesion;
    //POPUP ELIMINAR DIRECCIÓN
    private Dialog popupEliminarDireccion;
    public Button popupEliminarDireccionNo, popupEliminarDireccionEliminar;
    private TextView tvPopupEliminarDireccion;
    //POPUP VERIFICAR SERVICIO
    private Dialog popupVerificarServicio;
    public Button popupVerificarServicioCancelar, popupVerificarServicioConfirmar;
    //private TextView tvPopupVerificarServicio;


    public Popup(Context context, Context contextApp, View error) {
        this.context = context;
        this.contextApp = contextApp;
        this.error = error;
        pressButton = false;
        declaration();
    }

    /*
    public Popup(Context context, Context contextApp) {
        this.context = context;
        this.contextApp = contextApp;
        pressButton = false;
        declaration();
    }*/

    private void declaration()
    {
        //POPUP ERROR
        popupError = new BottomSheetDialog(context, R.style.Theme_Design_BottomSheetDialog);
        View popupErrorView = LayoutInflater.from(contextApp).inflate(R.layout.popup_error, (ViewGroup) error);
        popupError.setContentView(popupErrorView);
        btnPoupErrorAceptar = popupError.findViewById(R.id.btnPoupErrorAceptar);
        assert btnPoupErrorAceptar != null;
        btnPoupErrorAceptar.setOnClickListener(v ->popupError.dismiss());
        //tvPoupError = popupError.findViewById(R.id.tvPoupError);

        //POPUP AddAcc
        popupAddAcc = new BottomSheetDialog(context, R.style.Theme_Design_BottomSheetDialog);
        View popupAddAccView = LayoutInflater.from(contextApp).inflate(R.layout.popup_error, (ViewGroup) error);
        popupAddAcc.setContentView(popupAddAccView);
        btnPoupAddAccAceptar = popupAddAcc.findViewById(R.id.btnPoupAddAccAceptar);
        //tvPoupAddAcc = popupAddAcc.findViewById(R.id.tvPoupError); // error

        ///popup cerrar sesion
        popupCerrarSesion = new Dialog(context);
        popupCerrarSesion.setContentView(R.layout.popup_cerrar_sesion);
        popupCerrarSesion.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupCerrarSesionCancelar = popupCerrarSesion.findViewById(R.id.popupCerrarSesionCancelar);
        popupCerrarSesionSalir = popupCerrarSesion.findViewById(R.id.popupCerrarSesionSalir);
        tvPopupCerrarSesion = popupCerrarSesion.findViewById(R.id.tvCerrarSesion);

        ///popup eliminar dirección
        popupEliminarDireccion = new Dialog(context);
        popupEliminarDireccion.setContentView(R.layout.popup_eliminar_direccion);
        popupEliminarDireccion.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //verificar = findViewById(R.id.button_verificar);
        popupEliminarDireccionNo = popupEliminarDireccion.findViewById(R.id.popupEliminarDireccionNo);
        popupEliminarDireccionEliminar = popupEliminarDireccion.findViewById(R.id.popupEliminarDireccionEliminar);
        tvPopupEliminarDireccion = popupEliminarDireccion.findViewById(R.id.tvPopupEliminarDireccion);
        //

        //Popup Verificar Servicio
        popupVerificarServicio = new Dialog(context);
        popupVerificarServicio.setContentView(R.layout.popup_verificar_servicio);
        popupVerificarServicio.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupVerificarServicioCancelar = popupVerificarServicio.findViewById(R.id.popupVerificarServicioCancelar);
        popupVerificarServicioConfirmar = popupVerificarServicio.findViewById(R.id.popupVerificarServicioConfirmar);
        //
    }

    //POPUP ERROR
    @SuppressLint("SetTextI18n")
    public void setPopupError(String msg)
    {
        btnPoupErrorAceptar.setVisibility(View.VISIBLE);
        btnPoupAddAccAceptar.setVisibility(View.GONE);
        btnPoupErrorAceptar.setText("Aceptar");
        TextView tvErrorPopup = popupError.findViewById(R.id.tvPoupError);
        assert tvErrorPopup != null;
        tvErrorPopup.setText(msg);
        popupError.show();
    }

    //POPUP AddAcc
    @SuppressLint("SetTextI18n")
    public void setPopupAddAcc(String msg)
    {
        btnPoupAddAccAceptar.setVisibility(View.GONE);
        btnPoupAddAccAceptar.setVisibility(View.VISIBLE);
        TextView tvAddAccPopup = popupAddAcc.findViewById(R.id.tvPoupError);
        assert tvAddAccPopup != null;
        tvAddAccPopup.setText(msg);
        popupAddAcc.show();

        //btnPoupAddAccAceptar.setOnClickListener(v ->popupAddAcc.dismiss());

    }

    //POPUP Cerrar Sesión
    @SuppressLint("SetTextI18n")
    public void setPopupCerrarSesion(String nombre, String apellido)
    {
        if(apellido.equals(""))
            tvPopupCerrarSesion.setText(nombre + ", estás a punto de cerrar la sesión en la aplicación de Zeta Gas.");
        else
            tvPopupCerrarSesion.setText(nombre + " "+ apellido + ", estás a punto de cerrar la sesión en la aplicación de Zeta Gas.");

        popupCerrarSesionCancelar.setOnClickListener(v -> popupCerrarSesion.dismiss());

        popupCerrarSesion.show();
    }

    @SuppressLint("SetTextI18n")
    public void setEliminarDireccion(String etiqueta)
    {
        tvPopupEliminarDireccion.setText("¿Estás seguro que deseas eliminar esta dirección con la etiqueta "+ etiqueta +"?");
        popupEliminarDireccion.show();

        popupEliminarDireccionNo.setOnClickListener(v -> popupEliminarDireccion.dismiss());
    }

    @SuppressLint("SetTextI18n")
    public void setPopupConfirmar()
    {
        popupVerificarServicio.show();
    }

    public Dialog getPopupVerificarServicio()
    {
        return popupVerificarServicio;
    }

    public void hidePopupError()
    {
        popupError.dismiss();
    }

    public void hidePopupAddAcc()
    {
        popupAddAcc.dismiss();
    }

    public void hidePopupCerrarSesion()
    {
        popupCerrarSesion.dismiss();
    }

    public void hidePopupDireccion()
    {
        popupEliminarDireccion.dismiss();
    }

    public void hidePopupConfirmar()
    {
        popupVerificarServicio.dismiss();
    }

}
