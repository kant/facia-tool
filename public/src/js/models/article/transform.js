import _ from 'underscore';
import {getViewUrl, getOphanUrl} from 'models/article/links';
import deepGet from 'utils/deep-get';
import getMediaMainImage from 'utils/get-media-main-image';

export function getMainMediaType(contentApiArticle) {
    return _.chain(contentApiArticle.elements).where({relation: 'main'}).pluck('type').first().value();
}

export function getPrimaryTag(contentApiArticle) {
    return _.chain(contentApiArticle.tags).pluck('webTitle').first().value();
}

export function getContributorImage(contentApiArticle) {
    var contributors = _.chain(contentApiArticle.tags).where({type: 'contributor'});

    return contributors.value().length === 1 ? contributors.pluck('bylineLargeImageUrl').first().value() : undefined;

}

export function isPremium(contentApiArticle) {
    return contentApiArticle.fields.membershipAccess === 'members-only' ||
        contentApiArticle.fields.membershipAccess === 'paid-members-only' ||
        !!_.find(contentApiArticle.tags, {id: 'news/series/looking-back'});
}

export default function capiToInternalState(opts, article) {
    article.state.sectionName(article.props.sectionName());
    article.state.primaryTag(getPrimaryTag(opts));
    article.state.imageCutoutSrcFromCapi(getContributorImage(opts));
    article.state.imageSrcFromCapi(getMediaMainImage(opts));
    article.state.hasMainVideo(getMainMediaType(opts) === 'video');
    article.state.tone(opts.frontsMeta && opts.frontsMeta.tone);
    article.state.viewUrl(getViewUrl(article));
    article.state.ophanUrl(getOphanUrl(opts.webUrl));
    article.state.premium(isPremium(opts));
    if (deepGet(opts, '.fields.liveBloggingNow') === 'true') {
        article.state.isLiveBlog(true);
    }
    article.state.capiId(opts.capiId);
    article.state.shortUrl(opts.fields.shortUrl);
}