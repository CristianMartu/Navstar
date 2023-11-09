package com.myapplication.navstar;

import android.content.Context;

public class DatabaseSupportSingleton {
    private static com.myapplication.navstar.DatabaseSupport databaseSupport;

    private DatabaseSupportSingleton() {
    }

    public static com.myapplication.navstar.DatabaseSupport getInstance(Context context) {
        if (databaseSupport == null) {
            databaseSupport = new com.myapplication.navstar.DatabaseSupport(context);
        }
        return databaseSupport;
    }
}
