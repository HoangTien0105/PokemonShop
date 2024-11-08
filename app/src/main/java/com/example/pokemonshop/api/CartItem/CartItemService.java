package com.example.pokemonshop.api.CartItem;

import com.example.pokemonshop.model.CartItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CartItemService {
    @POST("CartItem")
    Call<Void> AddToCart(@Body CartItem item);

    @GET("CartItem/{CustomerId}")
    Call<List<CartItem>> getCartFromCustomer(@Path("CustomerId") int id);

    @PUT("CartItem/Quantity")
    Call<Void> updateItemQuantity(
            @Query("CartId") int cartId,
            @Query("Quantity") int quantity
    );


    @DELETE("CartItem/{id}")
    Call<Void> deleteItem(@Path("id") int id);
}
