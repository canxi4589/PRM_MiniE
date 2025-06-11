package com.example.mini_ecom;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final Context context;
    private final List<TransactionItem> transactions;
    private final SimpleDateFormat dateFormat;

    public TransactionAdapter(Context context, List<TransactionItem> transactions) {
        this.context = context;
        this.transactions = transactions;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionItem transaction = transactions.get(position);
        Log.d("TransactionBind", "Binding transaction: " + transaction.getTransactionId());
        // Set transaction data
        holder.transactionIdTextView.setText(transaction.getTransactionId());
        holder.transactionAmountTextView.setText(transaction.getFormattedAmount());
        holder.transactionDateTextView.setText(dateFormat.format(transaction.getTransactionDate()));
        holder.transactionItemsCountTextView.setText(transaction.getProductCountText());

        // Set status and icon with styling
        String status = transaction.getStatus();
        holder.transactionStatusTextView.setText(status);
        setStatusStyle(holder.transactionStatusTextView, status);

        // Click to details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransactionDetailActivity.class);
            intent.putExtra("transaction_id", transaction.getTransactionId());
            intent.putExtra("transaction_amount", transaction.getTotalAmount());
            intent.putExtra("transaction_date", transaction.getTransactionDate().getTime());
            intent.putExtra("transaction_status", status);
            intent.putExtra("payment_method", transaction.getPaymentMethod());
            intent.putExtra("item_count", transaction.getProductCount());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    private void setStatusStyle(TextView statusView, String status) {
        switch (status.toLowerCase()) {
            case "success":
                statusView.setTextColor(ContextCompat.getColor(context, R.color.success));
                break;
            case "failed":
                statusView.setTextColor(ContextCompat.getColor(context, R.color.error));
                break;
            case "pending":
                statusView.setTextColor(ContextCompat.getColor(context, R.color.warning));
                break;
            default:
                statusView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                break;
        }
    }
    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView transactionIdTextView;
        TextView transactionStatusTextView;
        TextView transactionAmountTextView;
        TextView transactionDateTextView;
        TextView transactionItemsCountTextView;
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionIdTextView = itemView.findViewById(R.id.transactionIdTextView);
            transactionStatusTextView = itemView.findViewById(R.id.transactionStatusTextView);
            transactionAmountTextView = itemView.findViewById(R.id.transactionAmountTextView);
            transactionDateTextView = itemView.findViewById(R.id.transactionDateTextView);
            transactionItemsCountTextView = itemView.findViewById(R.id.transactionItemsCountTextView);
        }
    }
}
