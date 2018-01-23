package me.nkkumawat.chatzzz.UI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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

import me.nkkumawat.chatzzz.Adapters.ContactsAdapter;
import me.nkkumawat.chatzzz.Model.Contacts;
import me.nkkumawat.chatzzz.R;

/**
 * Created by sonu on 20/1/18.
 */

public class FragmentContacts extends Fragment {

    private ListView contactList;
    private ContactsAdapter contactsAdapter;
    private ArrayList<Contacts> contactDetails;

    public FragmentContacts() {
    }

    public static FragmentContacts newInstance() {
        return new FragmentContacts();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        contactList = (ListView) rootView.findViewById(R.id.contactsList);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contactDetails = new ArrayList<>();

        getContacts();

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

    public void getContacts() {



        SharedPreferences preferences = getActivity().getSharedPreferences("contacts", Context.MODE_PRIVATE);
        try {
            String res = preferences.getString("contacts", null);
            Log.d("hammer"  ,res + "        n");
            JSONObject contacts = new JSONObject(preferences.getString("contacts", "{}"));
            JSONArray jsonArray = new JSONArray(contacts.getString("body"));
            for (int x = 0; x < jsonArray.length(); x++) {
                JSONObject object = jsonArray.getJSONObject(x);
                Contacts contact = new Contacts(object.getString("mobile"), object.getString("mobile"));
                contactDetails.add(contact);
            }
//           contactsAdapter.notifyDataSetChanged();
            Log.d("json" , contacts.toString());
        }
        catch (Exception e) {
            Log.d("nkkkkkkkkkkkk" , e.toString());
            e.printStackTrace();
        }
    }
}