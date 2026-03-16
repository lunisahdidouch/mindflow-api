package lunis.work.mindflow.study;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lunis.work.mindflow.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class StudySetService {
    private final StudySetRepository studySetRepository;
    private final FlashcardRepository flashcardRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final PracticeTestRepository practiceTestRepository;
    private final LearningPlanRepository learningPlanRepository;
    private final ChatMessageRepository chatMessageRepository;

    public StudySetService(
            StudySetRepository studySetRepository,
            FlashcardRepository flashcardRepository,
            QuizQuestionRepository quizQuestionRepository,
            PracticeTestRepository practiceTestRepository,
            LearningPlanRepository learningPlanRepository,
            ChatMessageRepository chatMessageRepository) {
        this.studySetRepository = studySetRepository;
        this.flashcardRepository = flashcardRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.practiceTestRepository = practiceTestRepository;
        this.learningPlanRepository = learningPlanRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public List<StudyDtos.StudySetListItem> listStudySets(Long userId) {
        return studySetRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(studySet -> new StudyDtos.StudySetListItem(
                        studySet.getId(),
                        studySet.getTopic(),
                        studySet.getSummary(),
                        studySet.getCreatedAt(),
                        flashcardRepository.countByStudySetId(studySet.getId()),
                        quizQuestionRepository.countByStudySetId(studySet.getId())))
                .collect(Collectors.toList());
    }

    public StudySet getRequiredStudySet(Long studySetId, Long userId) {
        return studySetRepository.findByIdAndUserId(studySetId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Study set niet gevonden."));
    }

    public StudyDtos.StudySetOverview getOverview(Long studySetId, Long userId) {
        return toOverview(getRequiredStudySet(studySetId, userId));
    }

    public List<StudyDtos.FlashcardView> getFlashcards(Long studySetId, Long userId) {
        getRequiredStudySet(studySetId, userId);
        return flashcardRepository.findByStudySetIdAndStudySetUserIdOrderByIdAsc(studySetId, userId).stream()
                .map(card -> new StudyDtos.FlashcardView(card.getId(), card.getFront(), card.getBack()))
                .toList();
    }

    public List<StudyDtos.QuizQuestionView> getQuiz(Long studySetId, Long userId) {
        getRequiredStudySet(studySetId, userId);
        return quizQuestionRepository.findByStudySetIdAndStudySetUserIdOrderByIdAsc(studySetId, userId).stream()
                .map(question -> new StudyDtos.QuizQuestionView(
                        question.getId(),
                        question.getQuestion(),
                        question.getOptions(),
                        question.getCorrectAnswerIndex(),
                        question.getExplanation()))
                .toList();
    }

    public StudyDtos.PracticeTestView getLatestPracticeTest(Long studySetId, Long userId) {
        PracticeTest test = practiceTestRepository
                .findFirstByStudySetIdAndStudySetUserIdOrderByCreatedAtDesc(studySetId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nog geen practice test beschikbaar."));
        return toPracticeTest(test);
    }

    public StudyDtos.LearningPlanView getLatestLearningPlan(Long studySetId, Long userId) {
        LearningPlan plan = learningPlanRepository
                .findFirstByStudySetIdAndStudySetUserIdOrderByCreatedAtDesc(studySetId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Nog geen leerplan beschikbaar."));
        return toLearningPlan(plan);
    }

    public List<StudyDtos.ChatMessageView> getChatMessages(Long studySetId, Long userId) {
        getRequiredStudySet(studySetId, userId);
        return chatMessageRepository.findByStudySetIdAndStudySetUserIdOrderByCreatedAtAsc(studySetId, userId).stream()
                .map(message -> new StudyDtos.ChatMessageView(
                        message.getId(), message.getRole(), message.getContent(), message.getCreatedAt()))
                .sorted(Comparator.comparing(StudyDtos.ChatMessageView::createdAt))
                .toList();
    }

    public StudyDtos.StudySetBundle getBundle(Long studySetId, Long userId) {
        return new StudyDtos.StudySetBundle(
                getOverview(studySetId, userId),
                getFlashcards(studySetId, userId),
                getQuiz(studySetId, userId));
    }

    public StudyDtos.StudySetOverview toOverview(StudySet studySet) {
        return new StudyDtos.StudySetOverview(
                studySet.getId(),
                studySet.getTopic(),
                studySet.getSummary(),
                studySet.getOriginalContent(),
                studySet.getCreatedAt());
    }

    public StudyDtos.PracticeTestView toPracticeTest(PracticeTest test) {
        return new StudyDtos.PracticeTestView(
                test.getId(),
                test.getTitle(),
                test.getDescription(),
                test.getFocus(),
                test.getQuestions(),
                test.getCreatedAt());
    }

    public StudyDtos.LearningPlanView toLearningPlan(LearningPlan plan) {
        return new StudyDtos.LearningPlanView(
                plan.getId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getSections(),
                plan.getCreatedAt());
    }
}
