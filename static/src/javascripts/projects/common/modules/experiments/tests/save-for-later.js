define([
    'bonzo',
    'qwery',
    'common/utils/detect',
    'common/utils/config',
    'common/utils/mediator',
    'common/utils/template',
    'common/modules/identity/api',
    'common/modules/loyalty/save-for-later',
    'text!common/views/identity/saved-for-later-profile-link.html',
    'common/modules/identity/api'
], function (
    bonzo,
    qwery,
    detect,
    config,
    mediator,
    template,
    id,
    SaveForLater,
    profileLinkTmp,
    Id
) {

    return function () {
        this.id = 'SaveForLater';
        this.start = '2015-04-09';
        this.expiry = '2015-07-09';
        this.author = 'Nathaniel Bennett';
        this.description = 'Internal test of save for later functionality';
        this.audience = 0.0;
        this.audienceOffset = 0;
        this.successMeasure = '';
        this.audienceCriteria = 'Interal only - we opt in';
        this.dataLinkNames = '';
        this.idealOutcome = '';
        this.showForSensitive = false;

        this.canRun = function () {
            return Id.isUserLoggedIn();
        };

        var init = function () {
            if (!/Network Front|Section/.test(config.page.contentType)) {
                var saveForLater = new SaveForLater();
                saveForLater.init();
            }
        };

        this.variants = [
            {
                id: 'variant',
                test: function () {
                    mediator.on('module:identity:api:loaded', init);

                    mediator.on('modules:profilenav:loaded', function () {
                        var popup = qwery('.popup--profile')[0];
                        bonzo(popup).append(bonzo.create(
                            template(profileLinkTmp.replace(/^\s+|\s+$/gm, ''), { idUrl: config.page.idUrl })
                        ));
                    });

                }
            }
        ];

        this.notInTest = function () {
            // On top of the A/B test, we always want to give pre-existing SFL
            // users the web feature.
            mediator.on('module:identity:api:loaded', function () {
                id.getSavedArticles().then(function (resp) {
                    var userHasSavedArticles = !!resp.savedArticles;

                    if (userHasSavedArticles) {
                        init();
                    }
                });
            });
        };
    };
});