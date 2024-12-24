package com.goldenrace.wallet.server.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(value = "/mock/api",
                produces = {"application/json"},
                consumes = {"application/json"})
public interface WalletApi {

    @PostMapping(path = "/wallet/keepalive")
    ResponseEntity<List<JsonNode>> sessionKeepAlive(@RequestBody List<JsonNode> bulkRequestKeep);

    @PostMapping(path = "/wallet/login")
    ResponseEntity<JsonNode> sessionLogin(@RequestBody JsonNode loginRequest);

    @PostMapping(path = "/wallet/logout", produces = {})
    ResponseEntity<Void> sessionLogout(@RequestBody List<JsonNode> bulkRequestLogout);

    @PostMapping(path = "/wallet/cancel")
    ResponseEntity<List<JsonNode>> walletCancel(@RequestBody List<JsonNode> bulkRequestCancel);

    @PostMapping(path = "/wallet/credit")
    ResponseEntity<List<JsonNode>> walletCredit(@RequestBody List<JsonNode> bulkRequestCredit);

    @PostMapping(path = "/wallet/payout")
    ResponseEntity<List<JsonNode>> walletPayout(@RequestBody List<JsonNode> bulkRequestPayout);

    @PostMapping(path = "/wallet/register")
    ResponseEntity<List<JsonNode>> walletRegister(@RequestBody List<JsonNode> bulkRequestRegister);

    @PostMapping(path = "/wallet/sell")
    ResponseEntity<List<JsonNode>> walletSell(@RequestBody List<JsonNode> bulkRequestSell);

    @PostMapping(path = "/wallet/solve")
    ResponseEntity<List<JsonNode>> walletSolve(@RequestBody List<JsonNode> bulkRequestSolve);

}

