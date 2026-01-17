package com.ondemanddeletionplatform.deletionworker.localinteg.connectors.dynamodb

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.GetItemRequest
import aws.sdk.kotlin.services.dynamodb.model.GetItemResponse
import aws.sdk.kotlin.services.dynamodb.model.PutItemRequest
import com.ondemanddeletionplatform.deletionworker.localinteg.testutil.dynamodb.DynamoDbIntegTestConstants
import com.ondemanddeletionplatform.deletionworker.localinteg.testutil.dynamodb.DynamoDbLocalStack
import com.ondemanddeletionplatform.deletionworker.localinteg.testutil.dynamodb.DynamoDbRepositoryUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll

abstract class DynamoDbIntegTest {
  protected lateinit var localstack: DynamoDbLocalStack
  protected lateinit var dynamoDb: DynamoDbClient
  protected val dynamoDbUtils: DynamoDbRepositoryUtils = DynamoDbRepositoryUtils()

  @BeforeAll
  fun setupLocalStack() {
    localstack = DynamoDbLocalStack()
    dynamoDb = localstack.dynamoDb
  }

  protected suspend fun validateDeleted(tableName: String, customerId: String, sortKeyVal: String?) {
    val getItemResult = getCustomerItem(tableName, customerId, sortKeyVal)
    assertNull(getItemResult.item)
  }

  protected suspend fun validateNotDeleted(tableName: String, customerIds: List<String>, sortKeyVal: String?) {
    customerIds.forEach {
      val getItemResult = getCustomerItem(tableName, it, sortKeyVal)
      val customerItem: Map<String, AttributeValue>? = getItemResult.item

      assertNotNull(customerItem)
      assertEquals(
        customerItem?.get(DynamoDbIntegTestConstants.PARTITION_KEY_NAME),
        AttributeValue.S(it)
      )
    }
  }

  protected suspend fun putItem(tableName: String, item: Map<String, AttributeValue>) {
    dynamoDb.putItem(
      PutItemRequest {
        this.tableName = tableName
        this.item = item
      }
    )
    println("Put item into table $tableName with key $item")
  }

  protected suspend fun getCustomerItem(tableName: String, customerId: String, sortKeyVal: String? = null): GetItemResponse {
    val tableKey = buildItemKey(customerId, sortKeyVal)
    val getItemResponse = dynamoDb.getItem(
      GetItemRequest {
        this.tableName = tableName
        key = tableKey
      }
    )
    println("GetItem(tableName: $tableName, partitionKey: $customerId, sortKey: $sortKeyVal): $getItemResponse")
    return getItemResponse
  }

  protected fun buildItemKey(partitionKeyVal: String, sortKeyVal: String?): Map<String, AttributeValue> {
    val keyValueMap = mutableMapOf(DynamoDbIntegTestConstants.PARTITION_KEY_NAME to AttributeValue.S(partitionKeyVal))
    if (sortKeyVal != null) {
      keyValueMap[DynamoDbIntegTestConstants.SORT_KEY_NAME] = AttributeValue.S(sortKeyVal)
    }
    return keyValueMap
  }
}
