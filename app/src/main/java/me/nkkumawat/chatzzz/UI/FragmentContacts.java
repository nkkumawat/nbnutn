package me.nkkumawat.chatzzz.UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import me.nkkumawat.chatzzz.Adapters.ContactsAdapter;
import me.nkkumawat.chatzzz.Database.DbHelper;
import me.nkkumawat.chatzzz.Model.Contacts;
import me.nkkumawat.chatzzz.Model.NewChatNumber;
import me.nkkumawat.chatzzz.R;
import me.nkkumawat.chatzzz.Utility.DatabaseChangedReceiver;

/**
 * Created by sonu on 20/1/18.
 */

public class FragmentContacts extends Fragment {

    private ListView contactList;
    private ContactsAdapter contactsAdapter;
    private ArrayList<Contacts> contactDetails;
    public  String typeOfInstance = "";
    private HashMap<String , String> chatHistoryHash ;
    public DbHelper dbHelper;
    public FragmentContacts() {
    }

    @SuppressLint("ValidFragment")
    public FragmentContacts(String type) {
        typeOfInstance = type;
    }

    public static FragmentContacts newInstance(String type) {
        return new FragmentContacts(type);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactList = (ListView) rootView.findViewById(R.id.contactsList);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DbHelper(getContext());
        contactDetails = new ArrayList<>();
        chatHistoryHash = new HashMap<String, String>();

        if(typeOfInstance.equals("history")) {
            chatHistoryHash.clear();
            getChatHistorys("from");
            getChatHistorys("to");
            getActivity().registerReceiver(mReceiver, new IntentFilter(DatabaseChangedReceiver.ACTION_DATABASE_CHANGED));
        }else {
            getContacts();
        }
        contactsAdapter = new ContactsAdapter(contactDetails, getContext());
        contactList.setAdapter(contactsAdapter);

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contacts contacts = contactDetails.get(position);
                Intent intent = new Intent(getActivity() , SingleChatWindow.class);
                intent.putExtra("name" , contacts.name);
                intent.putExtra("singlechat" , contacts.number);
                startActivity(intent);
//				Snackbar.make(view, contacts.name + "\n" + contacts.number, Snackbar.LENGTH_LONG)
//						.setAction("No action", null).show();
            }
        });
    }

    public void getChatHistorys(String col) {
        Cursor cursor = dbHelper.getChatMobileNumbers(col);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            if(!chatHistoryHash.containsKey(cursor.getString(0))) {
                Contacts newContact = dbHelper.getNameOfContactNo(cursor.getString(0));
                Contacts contact = new Contacts(newContact.name, cursor.getString(0) , newContact.pictureUrl);
                contactDetails.add(contact);
                chatHistoryHash.put(cursor.getString(0) ,newContact.name);
            }
            cursor.moveToNext();
        }
        cursor.close();
    }
    public void getContacts() {
        SharedPreferences preferences = getActivity().getSharedPreferences("contacts", Context.MODE_PRIVATE);
        DbHelper dbHelper = new DbHelper(getContext());
        try {
            String res = preferences.getString("contacts", null);
            Log.d("hammer"  ,res + "        n");
            JSONObject contacts = new JSONObject(preferences.getString("contacts", "{}"));
            JSONArray jsonArray = new JSONArray(contacts.getString("body"));
            for (int x = 0; x < jsonArray.length(); x++) {
                JSONObject object = jsonArray.getJSONObject(x);
                Contacts newContact = dbHelper.getNameOfContactNo(object.getString("mobile"));
                Contacts contact = new Contacts(newContact.name, object.getString("mobile"),newContact.pictureUrl);
                contactDetails.add(contact);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final DatabaseChangedReceiver mReceiver = new DatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            Contacts newContact = null;
            if(!chatHistoryHash.containsKey(NewChatNumber.mobileNo1)) {
                newContact = dbHelper.getNameOfContactNo(NewChatNumber.mobileNo1);
                Contacts contact = new Contacts(newContact.name, NewChatNumber.mobileNo1 , newContact.pictureUrl);
                contactsAdapter.add(contact);
                chatHistoryHash.put(NewChatNumber.mobileNo1, newContact.name);
            }
            if(!chatHistoryHash.containsKey(NewChatNumber.mobileNo2)) {
                newContact = dbHelper.getNameOfContactNo(NewChatNumber.mobileNo2);
                Contacts contact = new Contacts(newContact.name, NewChatNumber.mobileNo2 , newContact.pictureUrl);
                contactsAdapter.add(contact);
                chatHistoryHash.put(NewChatNumber.mobileNo2, newContact.name);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(typeOfInstance.equals("history")) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }
}