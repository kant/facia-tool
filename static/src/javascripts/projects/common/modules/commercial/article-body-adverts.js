define([
    'lodash/functions/once',
    'lodash/objects/cloneDeep',
    'common/utils/$',
    'common/utils/config',
    'common/utils/detect',
    'common/modules/article/spacefinder',
    'common/modules/commercial/create-ad-slot'
], function (
    once,
    cloneDeep,
    $,
    config,
    detect,
    spacefinder,
    createAdSlot
) {

    var ads = [],
        adNames = [['inline1', 'inline'], ['inline2', 'inline']],
        insertAdAtP = function (para) {
            if (para) {
                var adName = adNames[ads.length],
                    $ad    = $.create(createAdSlot(adName[0], adName[1]))
                                .insertBefore(para);
                ads.push($ad);
            }
        },
        init = function () {

            var breakpoint, rules, foBadgeRules;

            // is the switch off, or not an article, or a live blog
            if (!config.switches.standardAdverts || config.page.contentType !== 'Article' || config.page.isLiveBlog) {
                return false;
            }

            breakpoint = detect.getBreakpoint();
            rules      = {
                minAbove: detect.isBreakpoint({ max: 'tablet' }) ? 300 : 700,
                minBelow: 300,
                selectors: {
                    ' > h2': {minAbove: breakpoint === 'mobile' ? 20 : 0, minBelow: 250},
                    ' > *:not(p):not(h2)': {minAbove: 35, minBelow: 400},
                    ' .ad-slot': {minAbove: 500, minBelow: 500}
                }
            };

            if (
                config.page.sponsorshipType === 'foundation-supported' &&
                    config.page.isInappropriateForSponsorship === false
            ) {
                adNames.unshift(['fobadge', ['im', 'paid-for-badge']]);
                // more lenient rules for foundation badge
                foBadgeRules = cloneDeep(rules);
                foBadgeRules.minAbove = 300;
                insertAdAtP(spacefinder.getParaWithSpace(foBadgeRules));
            }
            if (config.page.hasInlineMerchandise) {
                adNames.unshift(['im', 'im']);
                insertAdAtP(spacefinder.getParaWithSpace(rules));
            }
            insertAdAtP(spacefinder.getParaWithSpace(rules));

            if (detect.isBreakpoint({ max: 'tablet' })) {
                insertAdAtP(spacefinder.getParaWithSpace(rules));
            }
        };

    return {

        init: once(init),

        destroy: function () {
            ads.forEach(function ($ad) {
                $ad.remove();
            });
        }

    };
});