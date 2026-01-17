package com.ondemanddeletionplatform.deletionworker.localinteg.testutil.dynamodb

import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeDefinition
import aws.sdk.kotlin.services.dynamodb.model.CreateTableRequest
import aws.sdk.kotlin.services.dynamodb.model.GlobalSecondaryIndex
import aws.sdk.kotlin.services.dynamodb.model.KeySchemaElement
import aws.sdk.kotlin.services.dynamodb.model.KeyType
import aws.sdk.kotlin.services.dynamodb.model.Projection
import aws.sdk.kotlin.services.dynamodb.model.ProjectionType
import aws.sdk.kotlin.services.dynamodb.model.ScalarAttributeType

class DynamoDbRepositoryUtils {
  suspend fun createTable(dynamoDb: DynamoDbClient, tableName: String, withSortKey: Boolean, gsis: List<GlobalSecondaryIndex>?) {
    val createTableRequest = buildCreateTableRequest(tableName, withSortKey, gsis)
    dynamoDb.createTable(createTableRequest)
    println("Created test table $tableName")
  }

  fun buildGlobalSecondaryIndexModel(gsiPartitionKey: String, gsiSortKey: String?): GlobalSecondaryIndex {
    var keySchema = mutableListOf(
      KeySchemaElement {
        attributeName = gsiPartitionKey
        keyType = KeyType.Hash
      }
    )
    if (gsiSortKey != null) {
      keySchema.add(
        KeySchemaElement {
          attributeName = gsiSortKey
          keyType = KeyType.Range
        }
      )
    }

    return GlobalSecondaryIndex {
      indexName = DynamoDbIntegTestConstants.GSI_NAME
      this.keySchema = keySchema
      provisionedThroughput = DynamoDbIntegTestConstants.PROVISIONED_THROUGHPUT
      projection = Projection {
        projectionType = ProjectionType.KeysOnly
      }
    }
  }

  fun buildCreateTableRequest(tableName: String, withSortKey: Boolean, gsis: List<GlobalSecondaryIndex>?): CreateTableRequest {
    var keySchema = mutableListOf(
      KeySchemaElement {
        attributeName = DynamoDbIntegTestConstants.PARTITION_KEY_NAME
        keyType = KeyType.Hash
      }
    )
    var attributeDefinitions = mutableListOf(
      AttributeDefinition {
        attributeName = DynamoDbIntegTestConstants.PARTITION_KEY_NAME
        attributeType = ScalarAttributeType.S
      }
    )

    if (withSortKey) {
      keySchema.add(
        KeySchemaElement {
          attributeName = DynamoDbIntegTestConstants.SORT_KEY_NAME
          keyType = KeyType.Range
        }
      )
      attributeDefinitions.add(
        AttributeDefinition {
          attributeName = DynamoDbIntegTestConstants.SORT_KEY_NAME
          attributeType = ScalarAttributeType.S
        }
      )
    }

    // Add attribute definitions for GSI keys
    gsis?.forEach { gsi ->
      gsi.keySchema.forEach { keyElement ->
        val attributeName = keyElement.attributeName
        if (attributeDefinitions.none { it.attributeName == attributeName }) {
          attributeDefinitions.add(
            AttributeDefinition {
              this.attributeName = attributeName
              attributeType = ScalarAttributeType.S
            }
          )
        }
      }
    }

    return CreateTableRequest {
      this.tableName = tableName
      this.keySchema = keySchema
      this.attributeDefinitions = attributeDefinitions
      globalSecondaryIndexes = gsis
      provisionedThroughput = DynamoDbIntegTestConstants.PROVISIONED_THROUGHPUT
    }
  }
}
