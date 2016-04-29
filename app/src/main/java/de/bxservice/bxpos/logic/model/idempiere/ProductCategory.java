package de.bxservice.bxpos.logic.model.idempiere;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import de.bxservice.bxpos.logic.daomanager.PosProductCategoryManagement;

/**
 * Created by diego on 5/11/15.
 */
public class ProductCategory {

    public static final String M_Product_Category_ID = "M_Product_Category_ID";

    private int productCategoryID;
    private String name;
    private int outputDeviceId;
    private List<MProduct> products = new ArrayList<>();
    private PosProductCategoryManagement productCategoryManager;

    public ProductCategory(){
    }

    public ProductCategory(int id, String name){
        productCategoryID = id;
        this.name = name;

    }

    public int getProductCategoryID() {
        return productCategoryID;
    }

    public void setProductCategoryID(int productCategoryID) {
        this.productCategoryID = productCategoryID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MProduct> getProducts() {
        return products;
    }

    public void setProducts(List<MProduct> products) {
        this.products = products;
    }

    public int getOutputDeviceId() {
        return outputDeviceId;
    }

    public void setOutputDeviceId(int outputDeviceId) {
        this.outputDeviceId = outputDeviceId;
    }

    /**
     * Save if the object does not exist it creates it
     * otherwise it updates it
     * @param ctx
     * @return
     */
    public boolean save(Context ctx) {
        productCategoryManager = new PosProductCategoryManagement(ctx);

        if (productCategoryManager.get(productCategoryID) == null)
            return createProductCategory();
        else
            return updateProductCategory();
    }

    private boolean updateProductCategory() {
        return productCategoryManager.update(this);
    }

    private boolean createProductCategory() {
        return productCategoryManager.create(this);
    }
}
