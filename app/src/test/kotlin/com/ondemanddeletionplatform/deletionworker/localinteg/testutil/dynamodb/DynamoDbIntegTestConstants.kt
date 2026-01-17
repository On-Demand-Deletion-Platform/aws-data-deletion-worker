package com.ondemanddeletionplatform.deletionworker.localinteg.testutil.dynamodb

import aws.sdk.kotlin.services.dynamodb.model.ProvisionedThroughput

object DynamoDbIntegTestConstants {
  private const val DDB_CAPACITY_UNITS = 10L

  const val AWS_REGION = "us-west-2"
  const val PARTITION_KEY_NAME = "id"
  const val SORT_KEY_NAME = "sortKey"
  const val SORT_KEY_VALUE = "sortKeyVal"
  const val SORT_KEY_VALUE_2 = "sortKeyVal2"
  const val GSI_NAME = "gsi"
  const val GSI_PARTITION_KEY_NAME = "gsiPartitionKey"
  const val GSI_SORT_KEY_NAME = "gsiSortKey"

  const val CUSTOMER_ID_1 = "customer1"
  const val CUSTOMER_ID_2 = "customer2"
  const val CUSTOMER_ID_3 = "customer3"
  const val CUSTOMER_ID_4 = "customer4"
  const val CUSTOMER_ID_5 = "customer5"

  const val WAIT_TIME_BETWEEN_DDB_OPERATIONS_MS = 100L

  val PROVISIONED_THROUGHPUT: ProvisionedThroughput = ProvisionedThroughput {
    readCapacityUnits = DDB_CAPACITY_UNITS
    writeCapacityUnits = DDB_CAPACITY_UNITS
  }
}
