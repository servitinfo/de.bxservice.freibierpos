package de.bxservice.bxpos.logic.webservices;

import android.content.Context;
import android.util.Log;

import org.idempiere.webservice.client.base.Enums;
import org.idempiere.webservice.client.base.Field;
import org.idempiere.webservice.client.net.WebServiceClient;
import org.idempiere.webservice.client.request.QueryDataRequest;
import org.idempiere.webservice.client.response.WindowTabDataResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import de.bxservice.bxpos.logic.model.Product;
import de.bxservice.bxpos.logic.model.ProductPrice;

/**
 * Created by Diego Ruiz on 9/11/15.
 */
public class ProductPriceWebServiceAdapter extends AbstractWSObject{

    //Associated record in Web Service Security in iDempiere
    private static final String SERVICE_TYPE = "QueryProductPrice";

    QueryDataRequest ws = new QueryDataRequest();
    List<ProductPrice> productPriceList;

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public void queryPerformed() {

        QueryDataRequest ws = new QueryDataRequest();
        ws.setServiceType(getServiceType());
        ws.setLogin(getLogin());

        WebServiceClient client = getClient();
        productPriceList = new ArrayList<ProductPrice>();

        try {
            WindowTabDataResponse response = client.sendRequest(ws);

            if ( response.getStatus() == Enums.WebServiceResponseStatus.Error ) {
                System.out.println(response.getErrorMessage());
            } else {

                Log.i("info", "Total rows: " + response.getNumRows());
                int priceListVersionId;
                int productId;
                int productPriceId;
                BigDecimal price;

                for (int i = 0; i < response.getDataSet().getRowsCount(); i++) {

                    Log.i("info", "Row: " + (i + 1));
                    priceListVersionId = 0;
                    productId = 0;
                    productPriceId = 0;
                    price = null;

                    for (int j = 0; j < response.getDataSet().getRow(i).getFieldsCount(); j++) {

                        Field field = response.getDataSet().getRow(i).getFields().get(j);
                        Log.i("info", "Column: " + field.getColumn() + " = " + field.getValue());

                        if( "M_PriceList_Version_ID".equalsIgnoreCase(field.getColumn()) )
                            priceListVersionId = Integer.valueOf(field.getValue());
                        else if ( ProductPrice.M_ProductPrice_ID.equalsIgnoreCase(field.getColumn()) )
                            productPriceId = Integer.valueOf(field.getValue());
                        else if ( Product.M_Product_ID.equalsIgnoreCase(field.getColumn()) )
                            productId = Integer.valueOf(field.getValue());
                        else if ( "PriceStd".equalsIgnoreCase(field.getColumn()) )
                            price = new BigDecimal(field.getValue());

                    }

                    if( priceListVersionId != 0 &&  productPriceId!= 0 &&
                            productId != 0 && price != null ){
                        ProductPrice p = new ProductPrice();
                        p.setProductID(productId);
                        p.setPriceListVersionID(priceListVersionId);
                        p.setProductPriceID(productPriceId);
                        p.setStdPrice(price);
                        productPriceList.add(p);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ProductPrice> getProductPriceList() {
        return productPriceList;
    }

    public void setProductPriceList(List<ProductPrice> productPriceList) {
        this.productPriceList = productPriceList;
    }
}