package me.nkkumawat.chatzzz.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.nkkumawat.chatzzz.Model.Contacts;
import me.nkkumawat.chatzzz.R;

/**
 * Created by sonu on 20/1/18.
 */

public class ContactsAdapter extends ArrayAdapter<Contacts> {
    private ArrayList<Contacts> contacts;

    public ContactsAdapter(ArrayList<Contacts> contacts, Context mContext) {
        super(mContext, R.layout.contacts_list, contacts);
        this.contacts = contacts;
    }
    @Override
    public void add(Contacts object) {
        contacts.add(object);
        super.add(object);
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.contacts_list, parent, false);
        }

        Contacts contact = contacts.get(position);
        ViewHolder viewHolder = new ViewHolder(convertView);
        viewHolder.name.setText(contact.name);
        viewHolder.number.setText(contact.number);
        Picasso.with(getContext())
                .load("https://png.pngtree.com/svg/20170602/0db185fb9c.png")
                .fit().centerInside()
                .placeholder(R.drawable.loading_fail)
                .error(R.drawable.loading_fail)
                .into(viewHolder.imageView);
        return convertView;
    }

    public class ViewHolder {
        TextView name;
        TextView number;
        ImageView imageView;

        ViewHolder(View view) {
            name = (TextView) view.findViewById(R.id.name);
            number = (TextView) view.findViewById(R.id.number);
            imageView = (ImageView) view.findViewById(R.id.userImage);
        }
    }
}
