/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.combineadresslists;

import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.impl.common.Levenshtein;

/**
 *
 * @author holgerklein
 */
public class Adresslist {

    private List<String> keys;
    private int emailIndex = 0;
    private List<List<String>> entries = new ArrayList<>();

    public Adresslist(List<String> keys) {
        this.keys = keys;
        getEmailIndex();
    }

    private void getEmailIndex() {
        float max = 0;
        for (String k : keys) {
            float sim = 1.0F / Levenshtein.distance("email", k);
            if (sim > max) {
                max = sim;
                emailIndex++;
            }
        }
        if (max < .8) {
            emailIndex = -1;
        }
    }

    public void addEntry(List<String> entry) {
        if (tryToCombine(entry)) {
            return;
        }
        entries.add(entry);
    }

    private boolean tryToCombine(List<String> newEntry) {
        if (emailIndex != -1) {
            String entryMail = newEntry.get(emailIndex);
            for (List<String> list : entries) {
                String existingEmail = list.get(emailIndex);
                if(existingEmail == null || entryMail == null) continue;
                if (existingEmail.equals(entryMail)) {
                    int pos = 0;
                    for (String newField : newEntry) {
                        if (list.get(pos).isEmpty()) {
                            list.set(pos, newField);
                        }
                        pos++;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<List<String>> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        String str = "";
        for (String s : keys) {
            str += "|" + s + "|";
        }
        str += "\n";
        for (List<String> list : entries) {
            for (String s : list) {
                str += "|" + s + "|";
            }
            str += "\n";
        }
        return str;
    }

}
