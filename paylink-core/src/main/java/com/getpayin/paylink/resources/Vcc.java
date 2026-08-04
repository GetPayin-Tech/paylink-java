package com.getpayin.paylink.resources;

import com.getpayin.paylink.model.VccChargeParams;
import com.getpayin.paylink.model.VccChargeResult;

/** Server-to-server raw-card charges. */
public interface Vcc {

    /** Charges raw card data directly (puts your server in PCI scope). */
    VccChargeResult charge(VccChargeParams params);

    VccChargeResult charge(VccChargeParams params, String idempotencyKey);
}
