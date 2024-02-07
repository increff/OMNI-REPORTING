package com.increff.omni.reporting.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api
@RestController
@RequestMapping(value = "/health")
public class HealthController {

    @ApiOperation(value = "For Health Check")
    @RequestMapping(method = RequestMethod.GET)
    public void healthCheck(){
    }

//    @ApiOperation(value = "For Health Check")
//    @RequestMapping(value = "/test", method = RequestMethod.POST)
//    public void healthCheck2(@RequestBody FileProviderForm form) throws ApiException {
//        PipelinePojo pipelinePojo = new PipelinePojo();
//        pipelinePojo.setType(form.getFileProviderType());
//        String creds = JsonUtil.serialize("{\n" +
//                "  \"type\": \"service_account\",\n" +
//                "  \"project_id\": \"nextscm-dev\",\n" +
//                "  \"private_key_id\": \"3f90fcad6a09f3ec97292577a12c41d3178333d7\",\n" +
//                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCV3+6sh7c6m42z\\nfpBJy52Bjscq63sYKcEinYar39IwUe1WOvFlSimLt+PJC8pEXxWfW+IwN1zIh5tR\\nC7pHIadLTmilPuY6IofpqbXObYZUweq1Rv3L9+zBH6ixPIoXUyILd6wmuQNaVQTY\\nZfoSlPyOqaIPxG4+RTKNcLOJZfIeL38+fFQC8Kq0Q7CYu+vxSQxeZWcOzkYNTaeI\\nJocBuMPtEj3/tOdy2Q93KqNLZF059+5pSX0YrusHgAy77WVzUiwQUGSCb4SkNuml\\nLXFGhOGIDkKlFZJl24hxv8VKZZmFvPlLtevB8UH2xUHvQmBjaqyYkyhzuLVIBKwp\\ngOPm8amrAgMBAAECggEAC060UqcAn3bZ94deenrWvM4ZX7cZd+a6kBKDdRczY/ri\\nZaWXzdUNEmIC2Qm/Lm5gEsBbbdjXcresc65nVPOIjClzwrfepdcyGuJ3eYYQ4vco\\nbpnXDxr67U0mrIyNPJpEkMnsHpXJ13hfn7X96oX8EgVhWUAwMxmEaWBt4zliTX2Z\\nU9hIc2sVZgfI/xm02kZ+b3j7us5wUwkKidoEgmBLhqUohCWiwVbgvr4OH4djFdFb\\nkLsA6x2UnAN7OlAMu3umLlnqkzefJIH1gC4leN800xj4h+EpFq32ER/77Q3AeUTL\\nZD1xCBW+I6ekUjAYf13RShEjnOK+9HM+23t+NtxR+QKBgQDHXYzmd0o5X8N3IKUA\\n87GswQ6Otf4zEBRKElBmpu4A/5y60lI4oxxi9Y5MMLW3VJoECjyX5/cI/6OYcqmQ\\nQwl5X1boMFPZRGnvBL673aWBtH5ZwlePw2YvCTJ+jdCBSJl/f+jHrFsH1pqPhplh\\nynVeDhmsyTfXbIRnh5I07SV9wwKBgQDAc0VOjEUJv7d+GjYj+Db1RWd0TZ7oRsBJ\\nP3G3cFCGxLqLrazhjU2c3TXilLbzs+Yqq8BC0ZUUJMxq37U3jxBOjxveKwIMV28P\\nLI2Vl9lOd1YLep0IDafD2kxZI/4/Z8LbGtgTxh/VJPefVrLZFrPho4mGH4wiyQhy\\nDZRndOPd+QKBgEZlRQxX0NCjfNyCnWDRq8opPR+tKX4UTmW9Q8CLbvzRRSE+hKPV\\nDH/w3vcOOAvtBpkomC63TKZTxgtdaXwz/5No08IW1nSgUJuCXGrvn7JF7lt5YPu1\\nbk5re/Xt01LjNLgtjR98P4RsBZVqXJyIJZs5dV1qi5o4iiQYXcrsddGLAoGBAKL/\\nPn0nmrWGFlCMvLw8V7fm/5dmwqcyfVvMi3D1hI6QgvsYruseNdUdGq18jXPdM52C\\neEVfoyc7f5MpRB/5YLNYzB6gSIzRZPPyJTzFLr0hn9P7FpMilavw8Wcyb/2d0VCn\\n7Mjark1lkQD4emQZsWLzkt/xSkyP/kIzNDVnsDNpAoGBAIU4/QsMsPpSdRtChvql\\nTTmLgQrKtjuOFLmk7ZoF4a5kHp8AR88ex+b2YKyEHTmhqmJ0yxgMQ0rtuKEeJ0R5\\nUxC206uLRs8e4Bfe/bzoNhCMT1hKej7xOZ/v1yAebDiW7D18ycyLqEN8uO7p2y3n\\n4B6C6zsBHeZF/PoroAOdx/tx\\n-----END PRIVATE KEY-----\\n\",\n" +
//                "  \"client_email\": \"dev-oltp@nextscm-dev.iam.gserviceaccount.com\",\n" +
//                "  \"client_id\": \"109422130006540346993\",\n" +
//                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
//                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
//                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
//                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/dev-oltp%40nextscm-dev.iam.gserviceaccount.com\"\n" +
//                "}");
//        Map<String, String> mp = new HashMap<>();
//        JsonUtil.serialize(mp);
//
//        pipelinePojo.setConfigs(form.getJsonObject().toString());
//        File file = new File("omni-reporting-files/report-105904_d5732c8b-fec1-4b95-b7c0-7a0ed5b30f83.csv");
//        ScheduleReportTask s = new ScheduleReportTask();
//        s.uploadScheduleFiles(pipelinePojo.getType(), pipelinePojo.getConfigs(), file);
//        //(pipelinePojo, file);
//    }
//
//    public void m(PipelinePojo pipelinePojo, File file) throws ApiException {
//        ScheduleReportTask s = new ScheduleReportTask();
//        s.uploadScheduleFiles(pipelinePojo.getType(), pipelinePojo.getDetails(), file);
//    }

}
