package com.example.huddleup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryEventListActivityK extends AppCompatActivity {

    private TextView tvCategoryTitle;
    private RecyclerView rvCategoryEvents;
    private Button btnEditCategoryFromDetail;
    private Button btnViewAllEvents;
    private FloatingActionButton fabAddEventToCategory;

    private DatabaseReference mDatabase;
    private String categoryId;
    private String categoryName;
    private List<EventK> categoryEvents = new ArrayList<>();
    private CategoryEventAdapterK categoryEventAdapter;

    private static final String TAG = "CatEventListActivityK";

    private final ActivityResultLauncher<Intent> editCategoryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadCategoryDetailsAndEvents();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_event_list);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        rvCategoryEvents = findViewById(R.id.rvCategoryEvents);
        btnEditCategoryFromDetail = findViewById(R.id.btnEditCategoryFromDetail);
        btnViewAllEvents = findViewById(R.id.btnViewAllEvents);
        fabAddEventToCategory = findViewById(R.id.fabAddEventToCategory);

        rvCategoryEvents.setLayoutManager(new LinearLayoutManager(this));

        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == null || categoryName == null) {
            Toast.makeText(this, "Error: Category data missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCategoryTitle.setText("Events in " + categoryName);

        categoryEventAdapter = new CategoryEventAdapterK(categoryEvents, position -> {
            EventK eventToRemove = categoryEvents.get(position);
            confirmRemoveEventFromCategory(eventToRemove);
        });
        rvCategoryEvents.setAdapter(categoryEventAdapter);

        loadCategoryDetailsAndEvents();

        btnEditCategoryFromDetail.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryEventListActivityK.this, CategoryCreationActivityK.class);
            intent.putExtra("categoryId", categoryId);
            editCategoryLauncher.launch(intent);
        });

        fabAddEventToCategory.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryEventListActivityK.this, CategoryCreationActivityK.class);
            intent.putExtra("categoryId", categoryId);
            editCategoryLauncher.launch(intent);
        });
    }

    private void loadCategoryDetailsAndEvents() {
        mDatabase.child("categories").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CategoryK category = dataSnapshot.getValue(CategoryK.class);
                if (category != null && category.getEvents() != null) {
                    categoryEvents.clear();
                    List<String> eventIdsInThisCategory = new ArrayList<>(category.getEvents().keySet());

                    if (eventIdsInThisCategory.isEmpty()) {
                        Toast.makeText(CategoryEventListActivityK.this, "No events in this category.", Toast.LENGTH_SHORT).show();
                        categoryEventAdapter.notifyDataSetChanged();
                        return;
                    }

                    for (String eventId : eventIdsInThisCategory) {
                        mDatabase.child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot eventSnapshot) {
                                EventK event = eventSnapshot.getValue(EventK.class);
                                if (event != null) {
                                    boolean found = false;
                                    for (EventK existingEvent : categoryEvents) {
                                        if (existingEvent.getId().equals(event.getId())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        categoryEvents.add(event);
                                    }
                                    categoryEventAdapter.notifyDataSetChanged();
                                } else {
                                    removeBrokenEventFromCategory(categoryId, eventId);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "Failed to load event " + eventId + ": " + databaseError.getMessage());
                            }
                        });
                    }
                } else {
                    categoryEvents.clear();
                    categoryEventAdapter.notifyDataSetChanged();
                    Toast.makeText(CategoryEventListActivityK.this, "No events in this category.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to load category events: " + databaseError.getMessage());
                Toast.makeText(CategoryEventListActivityK.this, "Failed to load category events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmRemoveEventFromCategory(EventK event) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Event from Category")
                .setMessage("Are you sure you want to remove '" + event.getName() + "' from this category? This will NOT delete the event from your overall event list.")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeEventFromCategory(event.getId());
                    }
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void removeEventFromCategory(String eventId) {
        mDatabase.child("categories").child(categoryId).child("events").child(eventId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CategoryEventListActivityK.this, "Event removed from category.", Toast.LENGTH_SHORT).show();
                    loadCategoryDetailsAndEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CategoryEventListActivityK.this, "Failed to remove event from category: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeBrokenEventFromCategory(String categoryId, String eventId) {
        mDatabase.child("categories").child(categoryId).child("events").child(eventId).removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Removed broken event ID " + eventId + " from category " + categoryId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to remove broken event ID " + eventId + ": " + e.getMessage()));
    }
}