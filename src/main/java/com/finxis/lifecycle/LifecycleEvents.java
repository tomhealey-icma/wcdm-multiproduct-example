package com.finxis.lifecycle;

import cdm.base.datetime.AdjustableOrAdjustedOrRelativeDate;
import cdm.base.math.NonNegativeQuantity;
import cdm.base.math.NonNegativeQuantitySchedule;
import cdm.base.math.metafields.FieldWithMetaNonNegativeQuantitySchedule;
import cdm.base.staticdata.party.PartyReferencePayerReceiver;
import cdm.event.common.*;
import cdm.observable.asset.PriceQuantity;
import cdm.product.common.settlement.ScheduledTransferEnum;
import com.rosetta.model.lib.records.Date;
import com.rosetta.model.metafields.FieldWithMetaDate;

import java.math.BigDecimal;
import java.util.List;

public class LifecycleEvents {

  public Instruction exerciseFxOption(Trade fxoTrade){

    PrimitiveInstruction pi = PrimitiveInstruction.builder()
            .setQuantityChange(QuantityChangeInstruction.builder()
                    .setChange(List.of(PriceQuantity.builder()
                                    .setQuantity(List.of(FieldWithMetaNonNegativeQuantitySchedule.builder()
                                                    .setValue(NonNegativeQuantitySchedule.builder()
                                                            .setValue(BigDecimal.valueOf(Long.parseLong("0")))))))))
            .setTransfer(TransferInstruction.builder()
                    .setTransferState(List.of(TransferState.builder()
                    .setTransfer(Transfer.builder()
                            .setQuantity(NonNegativeQuantity.builder()
                                    .setValue(BigDecimal.valueOf(Long.parseLong("1000"))))
                            .setSettlementDate(AdjustableOrAdjustedOrRelativeDate.builder()
                                    .setAdjustedDate(FieldWithMetaDate.builder()
                                            .setValue(Date.of(2025,10,30))))
                            .setPayerReceiver(PartyReferencePayerReceiver.builder()
                                    .setPayerPartyReferenceValue(fxoTrade.getParty().get(0))
                                    .setReceiverPartyReferenceValue(fxoTrade.getParty().get(1)))
                            .setTransferExpression(TransferExpression.builder()
                                    .setScheduledTransfer(ScheduledTransfer.builder()
                                            .setTransferType(ScheduledTransferEnum.EXERCISE)))))))
            .build();

    Instruction instruction = Instruction.builder()
            .setPrimitiveInstruction(pi)
            .build();

    return instruction;

  }
}
