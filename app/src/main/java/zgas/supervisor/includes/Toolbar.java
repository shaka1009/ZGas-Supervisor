package zgas.supervisor.includes;


import androidx.appcompat.app.AppCompatActivity;

import zgas.supervisor.R;

public class Toolbar {

    public static void show(AppCompatActivity activity, String title, boolean upButton) {
        androidx.appcompat.widget.Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }

    public static void show(AppCompatActivity activity, boolean upButton) {
        androidx.appcompat.widget.Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle("");
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }

    public static void showHome(AppCompatActivity activity, String title, boolean upButton) {
        androidx.appcompat.widget.Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }

    public static void showHome(AppCompatActivity activity, boolean upButton) {
        androidx.appcompat.widget.Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setHomeButtonEnabled(true);
        activity.getSupportActionBar().setTitle("");
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }


}
