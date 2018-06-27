package com.andrea.cabinetmapper;

public final class UpdateChecker {
    public static boolean isUpdated(){
        String APP_VERSION = "1.8.0";
        boolean result = false;

        UpdateCheckerThread uc = new UpdateCheckerThread();
        uc.start();
        try {
            uc.join();
        } catch (Exception e) {
        }

        if(uc.getResponse().equals(APP_VERSION))
            result = true;

        return result;
    }
}
