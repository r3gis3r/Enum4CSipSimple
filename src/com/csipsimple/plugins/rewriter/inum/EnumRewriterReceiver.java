package com.csipsimple.plugins.rewriter.inum;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import uk.nominet.DDDS.ENUM;
import uk.nominet.DDDS.Rule;

import java.util.Arrays;
import java.util.List;

public class EnumRewriterReceiver extends BroadcastReceiver {

    // Don't get the entire csipsimple api, we only need the action name
    private final static String ACTION_REWRITE_NUMBER = "com.csipsimple.phone.action.REWRITE_NUMBER";
    
    
    private ENUM mENUM; 
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if(ACTION_REWRITE_NUMBER.equalsIgnoreCase(action)) {
            // We'd like to rewrite for csipsimple :)
            String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Bundle result = getResultExtras(true);
            String rewritten = number;
            if(!TextUtils.isEmpty(number)) {

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String suffix = prefs.getString(CallHandlerConfig.KEY_ENUM_SUFFIX, "e164.arpa");
                String nbrPrefix = prefs.getString(CallHandlerConfig.KEY_NBR_PREFIX, "");
                List<String> supportedServices = Arrays.asList(prefs.getString(CallHandlerConfig.KEY_SERVICES_TO_MATCH, "sip").toLowerCase().split(CallHandlerConfig.SERVICES_TO_MATCH_SEPARATOR));
                
                if(number.startsWith(nbrPrefix)) {
                    number = number.substring(nbrPrefix.length());
                }
                
                mENUM = new ENUM(suffix);
                Rule[] rules = mENUM.lookup(number);
                
                for (Rule rule : rules) {
                    String[] services = rule.getService().toLowerCase().split("\\+");
                    
                    // check that resulting fields are valid
                    if (services.length != 2) {
                        continue;   // not x+y
                    }
                    if (!services[0].equals("e2u")) {
                        continue; // not E2U+...
                    }
                    if( supportedServices.indexOf(services[1]) != -1) {
                        // We have a winner !
                        number = rule.evaluate();
                        break;
                    }
                }
                
            }
            result.putString(Intent.EXTRA_PHONE_NUMBER, number);
        }
    }
    
}