package org.litetokens.core.db;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.litetokens.core.capsule.TransactionCapsule;
import org.litetokens.core.db.TransactionTrace.TimeResultType;

@Slf4j
public class PendingManager implements AutoCloseable {

  @Getter
  static List<TransactionCapsule> tmpTransactions = new ArrayList<>();
  Manager dbManager;

  public PendingManager(Manager db) {

    this.dbManager = db;
    tmpTransactions.addAll(db.getPendingTransactions());
    db.getPendingTransactions().clear();
    db.getSession().reset();
  }

  @Override
  public void close() {

    for (TransactionCapsule tx : PendingManager.tmpTransactions) {
      try {
        if (tx.getXltTrace() != null &&
            tx.getXltTrace().getTimeResultType().equals(TimeResultType.NORMAL)) {
          dbManager.getRepushTransactions().put(tx);
        }
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
        Thread.currentThread().interrupt();
      }
    }
    tmpTransactions.clear();

    for (TransactionCapsule tx : dbManager.getPoppedTransactions()) {
      try {
        if (tx.getXltTrace() != null &&
            tx.getXltTrace().getTimeResultType().equals(TimeResultType.NORMAL)) {
          dbManager.getRepushTransactions().put(tx);
        }
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
        Thread.currentThread().interrupt();
      }
    }
    dbManager.getPoppedTransactions().clear();
  }
}
