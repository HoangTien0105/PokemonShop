package com.example.pokemonshop.activity.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pokemonshop.R;
import com.example.pokemonshop.api.APIClient;
import com.example.pokemonshop.api.Category.CategoryRepository;
import com.example.pokemonshop.api.Category.CategoryService;
import com.example.pokemonshop.api.Product.ProductService;
import com.example.pokemonshop.model.Category;
import com.example.pokemonshop.model.Product;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddProductActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button btnAdd;

    private EditText productName, productDes, productPrice, productQuantity;

    private ImageView imageView;
    private List<Category> categoryList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    Spinner spinner;
    private Product product;
    private String base64Image = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbarAddProductAdmin);
        toolbar.setTitle("Thêm sản phẩm");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(AddProductActivity.this, ProductActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            AddProductActivity.this.finish();
        });

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Khởi tạo ImageView và đặt hình ảnh mặc định
        imageView = findViewById(R.id.product_image_admin_add);
        imageView.setImageResource(R.drawable.pikachu);

        // Xử lý khi click vào ImageView để chọn hình ảnh từ thiết bị
        imageView.setOnClickListener(v -> openImageChooser());

        btnAdd = findViewById(R.id.btnProductAdd);

        productName = findViewById(R.id.editProductName2);
        productDes = findViewById(R.id.editProductDescription2);
        productPrice = findViewById(R.id.editProductPrice2);
        productQuantity = findViewById(R.id.editProductQuantity2);

        spinner = findViewById(R.id.spinner);

        // Khởi tạo adapter cho Spinner
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Fetch categories and populate the spinner
        getCategories();

        // Xử lý khi người dùng chọn một mục từ Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected category ID
                int selectedCategoryId = categoryList.get(position).getCategoryId();

                // Xử lý khi người dùng nhấn nút Thêm sản phẩm
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try{
                            Product pro = new Product();
                            pro.setName(productName.getText().toString());
                            pro.setDescription(productDes.getText().toString());
                            pro.setPrice(Double.parseDouble(productPrice.getText().toString()));
                            pro.setQuantity(Integer.parseInt(productQuantity.getText().toString()));
                            pro.setCategoryId(selectedCategoryId);

                            // Nếu hình ảnh đã được chọn, thêm hình ảnh vào sản phẩm
                            if (base64Image != null) {
                                List<Product.ProductImage> images = new ArrayList<>();
                                images.add(new Product.ProductImage(null, base64Image));
                                pro.setImages(images);
                            }
                            ProductService service = APIClient.getClient().create(ProductService.class);
                            Call<Void> call = service.create(pro);
                            call.enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        Intent intent = new Intent(AddProductActivity.this, ProductActivity.class);
                                        Toast.makeText(getApplicationContext(), "Thêm thành công", Toast.LENGTH_SHORT).show();
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case where no item is selected if necessary
            }
        });
    }

    // Mở trình chọn hình ảnh từ thư viện
    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Vui lòng chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                // Lấy hình ảnh từ kết quả chọn và chuyển thành Bitmap
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap); // Hiển thị hình ảnh trong ImageView
                base64Image = encodeImageToBase64(bitmap); // Mã hóa hình ảnh sang chuỗi Base64
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // Mã hóa hình ảnh sang chuỗi Base64
    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    // Lấy danh sách các danh mục sản phẩm từ API và hiển thị vào Spinner
    private void getCategories() {
        CategoryService cateService = CategoryRepository.getCategoryService();
        Call<List<Category>> call = cateService.getAllCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful()) {
                    categoryList = response.body();
                    if (categoryList != null && !categoryList.isEmpty()) {
                        List<String> categoryNames = new ArrayList<>();
                        for (Category category : categoryList) {
                            categoryNames.add(category.getName());
                        }
                        adapter.clear();
                        adapter.addAll(categoryNames);
                        adapter.notifyDataSetChanged();

                        spinner.setSelection(0);
                    }
                } else {
                    Toast.makeText(AddProductActivity.this, "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e("AddProductActivity", "Error fetching categories", t);
                Toast.makeText(AddProductActivity.this, "Error fetching categories", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
