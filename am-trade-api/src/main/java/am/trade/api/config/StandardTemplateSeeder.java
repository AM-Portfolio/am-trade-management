package am.trade.api.config;

import am.trade.common.models.JournalTemplate;
import am.trade.common.models.TemplateField;
import am.trade.models.enums.JournalTemplateCategory;
import am.trade.models.enums.TemplateFieldType;
import am.trade.persistence.repository.JournalTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Seeds standard system journal templates idempotently (by name).
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StandardTemplateSeeder implements ApplicationRunner {

    private final JournalTemplateRepository templateRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedIfMissing("Breakout Trade", JournalTemplateCategory.TRADE_RECAP,
                "Plan and review breakout setups above resistance or below support.",
                breakoutFields());
        seedIfMissing("Pullback Trade", JournalTemplateCategory.TRADE_RECAP,
                "Plan and review pullback entries into trend continuation.",
                pullbackFields());
        seedIfMissing("Reversal Trade", JournalTemplateCategory.TRADE_RECAP,
                "Plan and review reversal setups at key swing points.",
                reversalFields());
        seedIfMissing("Opening Range", JournalTemplateCategory.PRE_MARKET,
                "Opening range breakout / fade planning checklist.",
                openingRangeFields());
        seedIfMissing("Swing Trade", JournalTemplateCategory.TRADE_RECAP,
                "Multi-day swing trade plan and review.",
                swingFields());
        seedIfMissing("Scalp Trade", JournalTemplateCategory.TRADE_RECAP,
                "Short-duration scalp checklist and review.",
                scalpFields());
        seedIfMissing("Options Trade", JournalTemplateCategory.TRADE_RECAP,
                "Options trade thesis, Greeks awareness, and review.",
                optionsFields());
    }

    private void seedIfMissing(String name, JournalTemplateCategory category, String description,
                               List<TemplateField> fields) {
        if (templateRepository.existsByName(name)) {
            log.debug("System template already exists: {}", name);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        JournalTemplate template = JournalTemplate.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .description(description)
                .category(category)
                .fields(fields)
                .isSystemTemplate(true)
                .isRecommended(true)
                .usageCount(0)
                .createdBy("system")
                .favoriteUserIds(new ArrayList<>())
                .tags(List.of("system", "standard"))
                .createdAt(now)
                .updatedAt(now)
                .build();

        templateRepository.save(template);
        log.info("Seeded system journal template: {}", name);
    }

    private List<TemplateField> checklist(String... labels) {
        List<TemplateField> fields = new ArrayList<>();
        int order = 1;
        for (String label : labels) {
            String id = label.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
            fields.add(TemplateField.builder()
                    .fieldId(id)
                    .fieldLabel(label)
                    .fieldType(TemplateFieldType.CHECKBOX)
                    .required(true)
                    .order(order++)
                    .helpText("Confirm before entry")
                    .build());
        }
        fields.add(TemplateField.builder()
                .fieldId("notes")
                .fieldLabel("Notes")
                .fieldType(TemplateFieldType.TEXTAREA)
                .required(false)
                .order(order)
                .placeholder("Additional context...")
                .build());
        return fields;
    }

    private List<TemplateField> breakoutFields() {
        return checklist(
                "Clear level identified",
                "Volume confirmation",
                "Risk defined (stop beyond level)",
                "Target mapped",
                "No conflicting news"
        );
    }

    private List<TemplateField> pullbackFields() {
        return checklist(
                "Trend direction clear",
                "Pullback to value (MA/VWAP/zone)",
                "Reversal trigger confirmed",
                "Stop beyond swing",
                "RR acceptable"
        );
    }

    private List<TemplateField> reversalFields() {
        return checklist(
                "Exhaustion / divergence present",
                "Key support/resistance test",
                "Reversal candle / structure",
                "Invalidation level set",
                "Position size reduced for reversal risk"
        );
    }

    private List<TemplateField> openingRangeFields() {
        return checklist(
                "Opening range marked",
                "Bias from premarket",
                "Breakout or fade plan ready",
                "Stop inside/outside OR defined",
                "News / catalyst check"
        );
    }

    private List<TemplateField> swingFields() {
        return checklist(
                "Higher timeframe alignment",
                "Entry zone defined",
                "Swing stop placed",
                "Targets / scaling plan",
                "Overnight / gap risk accepted"
        );
    }

    private List<TemplateField> scalpFields() {
        return checklist(
                "Tight risk defined",
                "Liquidity / spread acceptable",
                "Clear micro trigger",
                "Time stop set",
                "No revenge / oversize"
        );
    }

    private List<TemplateField> optionsFields() {
        return checklist(
                "Underlying thesis clear",
                "Expiry / DTE appropriate",
                "IV / premium justified",
                "Max loss defined",
                "Exit / roll plan ready"
        );
    }
}
