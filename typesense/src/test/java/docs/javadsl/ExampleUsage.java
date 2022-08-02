/*
 * Copyright (C) since 2016 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.javadsl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.typesense.*;
import akka.stream.alpakka.typesense.javadsl.FilterDeleteDocumentsQueryDsl;
import akka.stream.alpakka.typesense.javadsl.Typesense;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import spray.json.JsonReader;
import spray.json.JsonWriter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ExampleUsage {
  private static class MyDocument {
    final String id;
    final String name;

    MyDocument(String id, String name) {
      this.id = id;
      this.name = name;
    }
  }

  private static void example() {
    ActorSystem system = ActorSystem.create();

    // #settings
    String host = "http://localhost:8108";
    String apiKey = "Hu52dwsas2AdxdE";
    // val retrySettings: RetrySettings =
    //    RetrySettings(maxRetries = 6, minBackoff = 1.second, maxBackoff = 1.minute, randomFactor =
    // 0.2)
    RetrySettings retrySettings =
        RetrySettings.create(6, Duration.ofSeconds(1), Duration.ofMinutes(1), 0.2);
    TypesenseSettings settings = TypesenseSettings.create(host, apiKey, retrySettings);
    // #setings

    // #create collection
    Field field = Field.create("name", FieldType.string());
    List<Field> fields = Collections.singletonList(field);
    CollectionSchema collectionSchema = CollectionSchema.create("my_collection", fields);

    Source<CollectionSchema, NotUsed> createCollectionSource = Source.single(collectionSchema);
    Flow<CollectionSchema, TypesenseResult<CollectionResponse>, CompletionStage<NotUsed>>
        createCollectionFlow = Typesense.createCollectionFlow(settings);

    CompletionStage<TypesenseResult<CollectionResponse>> createCollectionResponse =
        createCollectionSource.via(createCollectionFlow).runWith(Sink.head(), system);
    // #create collection

    // #retrieve collection
    Source<RetrieveCollection, NotUsed> retrieveCollectionSource =
        Source.single(RetrieveCollection.create("my-collection"));
    Flow<RetrieveCollection, TypesenseResult<CollectionResponse>, CompletionStage<NotUsed>>
        retrieveCollectionFlow = Typesense.retrieveCollectionFlow(settings);

    CompletionStage<TypesenseResult<CollectionResponse>> retrievedCollectionResponse =
        retrieveCollectionSource.via(retrieveCollectionFlow).runWith(Sink.head(), system);
    // #retrieve collection

    JsonWriter<MyDocument> documentJsonWriter = null;
    JsonReader<MyDocument> documentJsonReader = null;

    // #index single document
    Source<IndexDocument<MyDocument>, NotUsed> indexSingleDocumentSource =
        Source.single(
            IndexDocument.create(
                "my-collection", new MyDocument(UUID.randomUUID().toString(), "Hello")));

    Flow<IndexDocument<MyDocument>, TypesenseResult<Done>, CompletionStage<NotUsed>>
        indexSingleDocumentFlow = Typesense.indexDocumentFlow(settings, documentJsonWriter);

    CompletionStage<TypesenseResult<Done>> indexSingleDocumentResponse =
        indexSingleDocumentSource.via(indexSingleDocumentFlow).runWith(Sink.head(), system);
    // #index single document

    // #index many documents
    Source<IndexManyDocuments<MyDocument>, NotUsed> indexManyDocumentsSource =
        Source.single(
            IndexManyDocuments.create(
                "my-collection",
                Collections.singletonList(new MyDocument(UUID.randomUUID().toString(), "Hello"))));

    Flow<
            IndexManyDocuments<MyDocument>,
            TypesenseResult<List<IndexDocumentResult>>,
            CompletionStage<NotUsed>>
        indexManyDocumentsFlow = Typesense.indexManyDocumentsFlow(settings, documentJsonWriter);

    CompletionStage<TypesenseResult<List<IndexDocumentResult>>> indexManyDocumentsResult =
        indexManyDocumentsSource.via(indexManyDocumentsFlow).runWith(Sink.head(), system);
    // #index many documents

    // #retrieve document
    Source<RetrieveDocument, NotUsed> retrieveDocumentSource =
        Source.single(RetrieveDocument.create("my-collection", UUID.randomUUID().toString()));

    Flow<RetrieveDocument, TypesenseResult<MyDocument>, CompletionStage<NotUsed>>
        retrieveDocumentFlow = Typesense.retrieveDocumentFlow(settings, documentJsonReader);

    CompletionStage<TypesenseResult<MyDocument>> retrieveDocumentResponse =
        retrieveDocumentSource.via(retrieveDocumentFlow).runWith(Sink.head(), system);
    // #retrieve document

    // #delete document
    Source<DeleteDocument, NotUsed> deleteDocumentSource =
        Source.single(DeleteDocument.create("my-collection", UUID.randomUUID().toString()));

    Flow<DeleteDocument, TypesenseResult<Done>, CompletionStage<NotUsed>> deleteDocumentFlow =
        Typesense.deleteDocumentFlow(settings);

    CompletionStage<TypesenseResult<Done>> deleteDocumentResponse =
        deleteDocumentSource.via(deleteDocumentFlow).runWith(Sink.head(), system);
    // #delete document

    // #delete documents by query
    Source<DeleteManyDocumentsByQuery, NotUsed> deleteDocumentsByQuerySource =
        Source.single(DeleteManyDocumentsByQuery.create("my-collection", "budget:>150"));

    Source<DeleteManyDocumentsByQuery, NotUsed> deleteDocumentsByQueryWithDslSource =
        Source.single(
            DeleteManyDocumentsByQuery.create(
                "my-collection",
                FilterDeleteDocumentsQueryDsl.inStringSet(
                    "id", Collections.singletonList(UUID.randomUUID().toString()))));

    Flow<
            DeleteManyDocumentsByQuery,
            TypesenseResult<DeleteManyDocumentsResult>,
            CompletionStage<NotUsed>>
        deleteDocumentsByQueryFlow = Typesense.deleteManyDocumentsByQueryFlow(settings);

    CompletionStage<TypesenseResult<DeleteManyDocumentsResult>> deleteDocumentsByQueryResult =
        deleteDocumentsByQuerySource.via(deleteDocumentsByQueryFlow).runWith(Sink.head(), system);
    // #delete documents by query
  }
}
