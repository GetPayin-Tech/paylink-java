package com.getpayin.paylink.internal;

import java.util.List;

/** One endpoint's signing contract: path, ordered fields, and country/state block. */
public record EndpointSpec(String path, List<FieldSpec> fields, boolean countryStateBlock) {
}
