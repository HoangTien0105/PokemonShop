package com.example.pokemonshop.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pokemonshop.R;
import com.example.pokemonshop.activity.customer.ProductDetailActivity;
import com.example.pokemonshop.activity.customer.fragments.CartFragment;
import com.example.pokemonshop.api.CartItem.CartItemRepository;
import com.example.pokemonshop.api.CartItem.CartItemService;
import com.example.pokemonshop.api.Product.ProductRepository;
import com.example.pokemonshop.api.Product.ProductService;
import com.example.pokemonshop.model.CartItem;
import com.example.pokemonshop.model.Product;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartItemRecyclerViewAdapter extends RecyclerView.Adapter<CartItemRecyclerViewAdapter.ViewHolder> {
    private List<CartItem> items;
    private Context context;
    private ProductService productService;
    private CartItemService cartItemService;
    private CartFragment cartFragment;

    public CartItemRecyclerViewAdapter(List<CartItem> items, Context context, CartFragment cartFragment) {
        this.items = items;
        this.context = context;
        this.cartFragment = cartFragment;
        this.cartItemService = CartItemRepository.getCartItemService();
        this.productService = ProductRepository.getProductService();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cart = items.get(position);
        holder.textViewName.setText(cart.getProductVIew().getName());
        holder.textViewPrice.setText(String.format("%.2f VND", cart.getProductVIew().getPrice()));
        holder.quantityEditText.setText(String.valueOf(cart.getQuantity()));
        holder.imageView.setImageResource(R.drawable.pikachu);

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = cart.getQuantity() + 1;
            cart.setQuantity(newQuantity);
            updateCartItemQuantity(cart.getItemId(), newQuantity, holder);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = cart.getQuantity() - 1;
            if (newQuantity > 0) {
                cart.setQuantity(newQuantity);
                updateCartItemQuantity(cart.getItemId(), newQuantity, holder);
            } else {
                Toast.makeText(context, "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnClickListener(v -> viewProductDetails(cart.getProductVIew().getProductId()));
        holder.btnDelete.setOnClickListener(v -> deleteCartItem(cart.getItemId(), position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void viewProductDetails(int productId) {
        Call<Product> call = productService.find(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    Intent intent = new Intent(context, ProductDetailActivity.class);
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        intent.putExtra("productImage", product.getImages().get(0).getBase64StringImage());
                    }
                    intent.putExtra("product", product);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Failed to load product details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteCartItem(int itemId, int position) {
        Call<Void> call = cartItemService.deleteItem(itemId);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    items.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, items.size());
                    Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    cartFragment.updateTotalPrice();
                } else {
                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartItemQuantity(int itemId, int quantity, ViewHolder holder) {
        Call<Void> call = cartItemService.updateItemQuantity(itemId, quantity);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Cập nhật số lượng thành công", Toast.LENGTH_SHORT).show();
                    cartFragment.updateTotalPrice();
                    holder.quantityEditText.setText(String.valueOf(quantity));
                } else {
                    Toast.makeText(context, "Cập nhật số lượng thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(context, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewPrice;
        public ImageView imageView;
        public EditText quantityEditText;
        public AppCompatButton btnDelete;
        public AppCompatButton btnIncrease;
        public AppCompatButton btnDecrease;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.tvItemName);
            textViewPrice = itemView.findViewById(R.id.tvItemPrice);
            quantityEditText = itemView.findViewById(R.id.tvItemQuantity);
            imageView = itemView.findViewById(R.id.imgItem);
            btnDelete = itemView.findViewById(R.id.btnDeleteItemCart);
            btnIncrease = itemView.findViewById(R.id.btnIncreaseQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecreaseQuantity);
        }
    }
}