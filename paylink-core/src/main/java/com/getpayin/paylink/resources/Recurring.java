package com.getpayin.paylink.resources;

import com.getpayin.paylink.model.CreateRecurringParams;
import com.getpayin.paylink.model.CreateRecurringResult;
import com.getpayin.paylink.model.MandateActionResult;
import com.getpayin.paylink.model.MandateStatusResult;

/** Recurring mandates (subscriptions). */
public interface Recurring {

    /** Creates a mandate and returns a checkout URL for the first (setup) charge. */
    CreateRecurringResult create(CreateRecurringParams params);

    CreateRecurringResult create(CreateRecurringParams params, String idempotencyKey);

    /** Queries a mandate's status. */
    MandateStatusResult status(String mandateId);

    /** Cancels a mandate. */
    MandateActionResult cancel(String mandateId);

    /** Pauses a mandate. */
    MandateActionResult pause(String mandateId);

    /** Resumes a paused mandate. */
    MandateActionResult resume(String mandateId);
}
