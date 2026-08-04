package com.getpayin.paylink.resources;

import com.getpayin.paylink.model.ChargeCardParams;
import com.getpayin.paylink.model.ChargeTokenResult;
import com.getpayin.paylink.model.RevokeTokenResult;
import com.getpayin.paylink.model.TokenizeCardParams;
import com.getpayin.paylink.model.TokenizeCardResult;

/** Card tokenization and stored-card charges. */
public interface Cards {

    /** Vaults a card and returns a reusable token. */
    TokenizeCardResult tokenize(TokenizeCardParams params);

    TokenizeCardResult tokenize(TokenizeCardParams params, String idempotencyKey);

    /** Charges a previously vaulted card. */
    ChargeTokenResult charge(ChargeCardParams params);

    ChargeTokenResult charge(ChargeCardParams params, String idempotencyKey);

    /** Revokes a vaulted card token. */
    RevokeTokenResult revoke(String cardToken);
}
