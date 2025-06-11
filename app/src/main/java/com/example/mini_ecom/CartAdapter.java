package com.example.mini_ecom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private OnCartItemChangeListener listener;

    public interface OnCartItemChangeListener {
        void onQuantityChanged();
        void onItemRemoved();
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = cartItem.getProduct();

        holder.nameTextView.setText(product.getName());
        holder.priceTextView.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
        holder.quantityTextView.setText(String.valueOf(cartItem.getQuantity()));
        holder.totalPriceTextView.setText(String.format(Locale.getDefault(), "$%.2f", cartItem.getTotalPrice()));
        holder.productImageView.setImageResource(product.getImageResource());

        holder.increaseButton.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(product.getId(), cartItem.getQuantity() + 1);
            notifyItemChanged(position);
            if (listener != null) {
                listener.onQuantityChanged();
            }
        });

        holder.decreaseButton.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                CartManager.getInstance().updateQuantity(product.getId(), cartItem.getQuantity() - 1);
                notifyItemChanged(position);
            } else {
                CartManager.getInstance().removeFromCart(product.getId());
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
            }
            if (listener != null) {
                listener.onQuantityChanged();
            }
        });

        holder.removeButton.setOnClickListener(v -> {
            CartManager.getInstance().removeFromCart(product.getId());
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
            if (listener != null) {
                listener.onItemRemoved();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView nameTextView;
        TextView priceTextView;
        TextView quantityTextView;
        TextView totalPriceTextView;
        Button increaseButton;
        Button decreaseButton;
        Button removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.cartItemImageView);
            nameTextView = itemView.findViewById(R.id.cartItemNameTextView);
            priceTextView = itemView.findViewById(R.id.cartItemPriceTextView);
            quantityTextView = itemView.findViewById(R.id.cartItemQuantityTextView);
            totalPriceTextView = itemView.findViewById(R.id.cartItemTotalPriceTextView);
            increaseButton = itemView.findViewById(R.id.increaseQuantityButton);
            decreaseButton = itemView.findViewById(R.id.decreaseQuantityButton);
            removeButton = itemView.findViewById(R.id.removeItemButton);
        }
    }
} 