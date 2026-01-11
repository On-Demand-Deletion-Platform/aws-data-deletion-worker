package com.ondemanddeletionplatform.deletionworker

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class DeletionWorkerTest {
  @Test
  fun deletionWorkerHasAGreeting() {
    val worker = DeletionWorker()
    assertNotNull(worker.greeting, "deletion worker should have a greeting")
  }

  @Test
  fun mainFunctionSucceeds() {
    main()
  }
}
