package com.medlink.app.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.medlink.app.models.Donation;
import com.medlink.app.models.MedicineRequest;
import com.medlink.app.models.Ngo;

public class DataRepository {

    private final FirebaseFirestore db;
    private final String currentUserId;

    public DataRepository() {
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = null;
        }
    }

    public interface ResultCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void submitDonation(Donation donation, ResultCallback<Void> callback) {
        if (currentUserId == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        donation.setDonorId(currentUserId);
        
        // 1. Add donation document
        db.collection("Donations")
                .add(donation)
                .addOnSuccessListener(documentReference -> {
                    // Update ID in the document itself for easier retrieval
                    donation.setDonationId(documentReference.getId());
                    documentReference.set(donation);
                    
                    // 2. Increment user's medicinesDonated counter
                    db.collection("Users").document(currentUserId)
                            .update("medicinesDonated", FieldValue.increment(1))
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void submitRequest(MedicineRequest request, ResultCallback<Void> callback) {
        if (currentUserId == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        request.setRequesterId(currentUserId);

        db.collection("Requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    request.setRequestId(documentReference.getId());
                    documentReference.set(request);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void seedNgosIfEmpty() {
        String[] names = {"Hope", "Care", "Life", "Health", "Red Cross", "Swast", "Smile", "Direct", "Rural", "Global", "City", "Unity", "Heart", "First", "Relief", "Support", "Alliance"};
        String[] suffixes = {"Foundation", "Org", "Care", "Health Hub", "Alliance", "Relief Group", "Clinic", "Trust", "Society", "Council"};
        String[] categories = {"Emergency", "General", "Specialized", "Pediatrics", "Cardiology", "Antibiotics", "Diabetes", "Mental Health"};
        String[] locations = {"Delhi", "Mumbai", "Pune", "Bangalore", "Bihar", "Goa", "Chennai", "Kolkata", "Hyderabad", "Assam"};
        String[] medicines = {
            "Paracetamol (500mg)", "Amoxicillin (250mg)", "Metformin (500mg)", 
            "Atorvastatin (20mg)", "Ibuprofen (400mg)", "Aspirin (100mg)", 
            "Azithromycin (500mg)", "Ciprofloxacin (500mg)", "Lisinopril (10mg)", 
            "Omeprazole (20mg)", "Insulin (10ml)", "Salbutamol (Inhaler)", 
            "Cetirizine (10mg)", "Amlodipine (5mg)", "Losartan (50mg)"
        };

        java.util.Random random = new java.util.Random();

        db.collection("NGOs").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        for (int i = 1; i <= 53; i++) {
                            String name = names[random.nextInt(names.length)] + " " + names[random.nextInt(names.length)] + " " + suffixes[random.nextInt(suffixes.length)];
                            String cat = categories[random.nextInt(categories.length)];
                            String loc = locations[random.nextInt(locations.length)];
                            double rating = 4.0 + (5.0 - 4.0) * random.nextDouble();
                            
                            Ngo ngo = new Ngo(name, cat, loc, Math.round(rating * 10.0) / 10.0, 0);
                            ngo.setVerified(random.nextBoolean());
                            ngo.setInventory(generateRandomInventory(medicines, random));
                            
                            db.collection("NGOs").add(ngo).addOnSuccessListener(doc -> {
                                ngo.setNgoId(doc.getId());
                                doc.set(ngo);
                            });
                        }
                    } else {
                        // Check for NGOs without inventory and update them
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Ngo ngo = doc.toObject(Ngo.class);
                            if (ngo.getInventory() == null || ngo.getInventory().isEmpty()) {
                                ngo.setInventory(generateRandomInventory(medicines, random));
                                db.collection("NGOs").document(doc.getId()).set(ngo);
                            }
                        }
                    }
                });
    }

    private java.util.Map<String, Integer> generateRandomInventory(String[] medicines, java.util.Random random) {
        java.util.Map<String, Integer> inventory = new java.util.HashMap<>();
        int medCount = 3 + random.nextInt(5); // 3 to 7 medicines
        for (int j = 0; j < medCount; j++) {
            String med = medicines[random.nextInt(medicines.length)];
            inventory.put(med, 2 + random.nextInt(19)); // 2 to 20 quantity
        }
        return inventory;
    }
}
