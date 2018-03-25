package bot.service;

import bot.entity.QaEntity;
import bot.repository.QaRepository;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKAnalyzer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Service
public class SearchService {

    private static Logger logger = Logger.getLogger(SearchService.class);

    @Resource
    private QaRepository qaRepository;

    private Directory directory;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private Analyzer ikAnalyzer;

    @PostConstruct
    public void setUp() throws IOException {
        //索引存放的位置，设置在当前目录中
//        directory = FSDirectory.open(Pathsths.get("indexDir/"));
        directory = new RAMDirectory();
        ikAnalyzer = new IKAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(new WhitespaceAnalyzer());
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        try (IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
            init(indexWriter);
        }
        //创建索引的读取器
        indexReader = DirectoryReader.open(directory);
        //创建一个索引的查找器，来检索索引库
        indexSearcher = new IndexSearcher(indexReader);
    }

    private void init(IndexWriter indexWriter) throws IOException {
        List<QaEntity> allQaEntities = qaRepository.findAll();
        for (QaEntity qaEntity : allQaEntities) {
            Document doc = new Document();
            doc.add(new StringField("ID", qaEntity.getId().toString(), Field.Store.YES));
            // 关键字用逗号分词即可  替换成一个空格方便处理
            String processKeywords = qaEntity.getKeywords().replaceAll("，", ",")
                    .replaceAll(",", " ").toLowerCase();
            printAnalyzerDoc(processKeywords);
            doc.add(new TextField("keywords", processKeywords, Field.Store.NO));
            indexWriter.addDocument(doc);
        }
    }

    @PreDestroy
    public void tearDown() throws Exception {
        indexReader.close();
    }

    /**
     * 执行查询，并打印查询到的记录数
     *
     * @param words
     * @throws IOException
     */
    public List<String> executeQuery(String words) throws IOException {
        QueryParser queryParser = new QueryParser("keywords", ikAnalyzer);
        Query query = null;
        TopDocs topDocs = null;
        try {
            query = queryParser.parse(words);
            topDocs = indexSearcher.search(query, 5);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //打印查询到的记录数
        logger.info("总共查询到" + topDocs.totalHits + "个文档");
        List<String> ret = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //取得对应的文档对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            /*logger.info("id：" + document.get("ID"));*/
            /*logger.info("" + indexSearcher.explain(query, scoreDoc.doc));*/
            logger.info(document.get("ID") + " " + scoreDoc.score);
            ret.add(document.get("ID"));
        }
        return ret;
    }

    /**
     * 分词打印
     *
     * @param text
     * @throws IOException
     */
    public void printAnalyzerDoc(String text) throws IOException {
        try (Analyzer an = new WhitespaceAnalyzer();
             TokenStream tokenStream = an.tokenStream("keywords", new StringReader(text))) {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                logger.info(charTermAttribute.toString());
            }
            tokenStream.end();
        }
    }
}