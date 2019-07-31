package com.example.eduardorodriguez.comeaqui.profile.edit_profile.edit_account_details.payment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eduardorodriguez.comeaqui.R;
import com.example.eduardorodriguez.comeaqui.objects.PaymentObject;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodsAdapter extends RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder>{

    private List<PaymentObject> listPaymentMethods = new ArrayList<>();
    private Context c;
    StorageReference firebaseStorage;

    public PaymentMethodsAdapter(Context c, ArrayList<PaymentObject> paymentObjects) {
        this.c = c;
        listPaymentMethods = paymentObjects;
    }

    public void addPaymentMethod(PaymentObject m){
        listPaymentMethods.add(m);
        notifyItemInserted(listPaymentMethods.size());
    }

    @Override
    public PaymentMethodsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payment_method_element, parent, false);
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        return new PaymentMethodsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PaymentMethodsAdapter.ViewHolder holder, int position) {
        holder.mItem = listPaymentMethods.get(position);
        holder.paymentType.setText(holder.mItem.card_type);
        holder.paymentInfo.setText("Ending " + holder.mItem.card_number.substring(holder.mItem.card_number.length() - 4));
    }

    @Override
    public int getItemCount() {
        return listPaymentMethods != null ? listPaymentMethods.size(): 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView paymentType;
        public final TextView paymentInfo;
        public final ImageView imageView;
        public PaymentObject mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            paymentType = view.findViewById(R.id.payment_type);
            paymentInfo = view.findViewById(R.id.payment_info);
            imageView = view.findViewById(R.id.image);
        }
    }
}