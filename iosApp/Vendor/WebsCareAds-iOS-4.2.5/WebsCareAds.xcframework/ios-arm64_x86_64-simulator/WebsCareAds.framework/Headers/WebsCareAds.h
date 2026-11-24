#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class UIView, UIViewController, WCAAdConfig, WCAAdEnvironment, WCAAdError, WCAAdEvent, WCAAdEventAttempt, WCAAdEventClicked, WCAAdEventDismissed, WCAAdEventFailed, WCAAdEventImpression, WCAAdEventLoaded, WCAAdEventPaid, WCAAdEventRequested, WCAAdFormat, WCAAdFormatCompanion, WCAAdNetworkConfig, WCAAdPlacementConfig, WCAAdPolicy, WCAAdPolicyCompanion, WCAAdPolicyParser, WCAAdProviderType, WCAAdProviderTypeCompanion, WCAAdType, WCAAttemptOutcome, WCACollapsiblePosition, WCAKotlinArray<T>, WCAKotlinEnum<E>, WCAKotlinEnumCompanion, WCAKotlinPair<__covariant A, __covariant B>, WCALogLevel, WCANativeSize, WCATestAdIds, WCAWebsCareAds;

@protocol WCAKotlinComparable, WCAKotlinIterator, WCAPolicySource;

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wincompatible-property-type"
#pragma clang diagnostic ignored "-Wnullability"

#pragma push_macro("_Nullable_result")
#if !__has_feature(nullability_nullable_result)
#undef _Nullable_result
#define _Nullable_result _Nullable
#endif

__attribute__((swift_name("KotlinBase")))
@interface WCABase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end

@interface WCABase (WCABaseCopying) <NSCopying>
@end

__attribute__((swift_name("KotlinMutableSet")))
@interface WCAMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end

__attribute__((swift_name("KotlinMutableDictionary")))
@interface WCAMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end

@interface NSError (NSErrorWCAKotlinException)
@property (readonly) id _Nullable kotlinException;
@end

__attribute__((swift_name("KotlinNumber")))
@interface WCANumber : NSNumber
- (instancetype)initWithChar:(char)value __attribute__((unavailable));
- (instancetype)initWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
- (instancetype)initWithShort:(short)value __attribute__((unavailable));
- (instancetype)initWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
- (instancetype)initWithInt:(int)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
- (instancetype)initWithLong:(long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
- (instancetype)initWithLongLong:(long long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
- (instancetype)initWithFloat:(float)value __attribute__((unavailable));
- (instancetype)initWithDouble:(double)value __attribute__((unavailable));
- (instancetype)initWithBool:(BOOL)value __attribute__((unavailable));
- (instancetype)initWithInteger:(NSInteger)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
+ (instancetype)numberWithChar:(char)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
+ (instancetype)numberWithShort:(short)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
+ (instancetype)numberWithInt:(int)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
+ (instancetype)numberWithLong:(long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
+ (instancetype)numberWithLongLong:(long long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
+ (instancetype)numberWithFloat:(float)value __attribute__((unavailable));
+ (instancetype)numberWithDouble:(double)value __attribute__((unavailable));
+ (instancetype)numberWithBool:(BOOL)value __attribute__((unavailable));
+ (instancetype)numberWithInteger:(NSInteger)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
@end

__attribute__((swift_name("KotlinByte")))
@interface WCAByte : WCANumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end

__attribute__((swift_name("KotlinUByte")))
@interface WCAUByte : WCANumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end

__attribute__((swift_name("KotlinShort")))
@interface WCAShort : WCANumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end

__attribute__((swift_name("KotlinUShort")))
@interface WCAUShort : WCANumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end

__attribute__((swift_name("KotlinInt")))
@interface WCAInt : WCANumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end

__attribute__((swift_name("KotlinUInt")))
@interface WCAUInt : WCANumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end

__attribute__((swift_name("KotlinLong")))
@interface WCALong : WCANumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end

__attribute__((swift_name("KotlinULong")))
@interface WCAULong : WCANumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end

__attribute__((swift_name("KotlinFloat")))
@interface WCAFloat : WCANumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end

__attribute__((swift_name("KotlinDouble")))
@interface WCADouble : WCANumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end

__attribute__((swift_name("KotlinBoolean")))
@interface WCABoolean : WCANumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdConfig")))
@interface WCAAdConfig : WCABase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));

/**
 * Apply a new remote policy with revision check and atomic update.
 */
- (void)applyPolicyPolicy:(WCAAdPolicy *)policy __attribute__((swift_name("applyPolicy(policy:)")));

/**
 * Resolves whether a placement is enabled considering:
 * 1. Local explicit override (local false ALWAYS wins)
 * 2. Active Remote Policy (remote false wins)
 * 3. Host predicate check (placementEnabledCheck)
 * 4. Default: true
 */
- (BOOL)isPlacementEnabledAdUnitId:(NSString *)adUnitId __attribute__((swift_name("isPlacementEnabled(adUnitId:)")));

/**
 * Locally enable or disable a specific placement.
 * Note: A local false (emergency OFF) always takes precedence over predicates or remote policies.
 */
- (void)setPlacementEnabledAdUnitId:(NSString *)adUnitId enabled:(BOOL)enabled __attribute__((swift_name("setPlacementEnabled(adUnitId:enabled:)")));
@property (readonly) WCAAdPolicy * _Nullable activePolicy __attribute__((swift_name("activePolicy")));
@property int32_t appOpenAdExpiryHours __attribute__((swift_name("appOpenAdExpiryHours")));
@property int64_t appOpenCooldownMs __attribute__((swift_name("appOpenCooldownMs")));

/** Where the current app-open cooldown came from: "host", "policy" or "default". */
@property (readonly) NSString *appOpenCooldownSource __attribute__((swift_name("appOpenCooldownSource")));
@property BOOL appOpenSplashEnabled __attribute__((swift_name("appOpenSplashEnabled")));
@property BOOL categoriesNativeEnabled __attribute__((swift_name("categoriesNativeEnabled")));
@property WCAInt * _Nullable defaultNativeLayout __attribute__((swift_name("defaultNativeLayout")));
@property WCANativeSize *defaultNativeSize __attribute__((swift_name("defaultNativeSize")));
@property BOOL emptyStateNativeEnabled __attribute__((swift_name("emptyStateNativeEnabled")));
@property BOOL enableConsent __attribute__((swift_name("enableConsent")));
@property WCAAdEnvironment *environment __attribute__((swift_name("environment")));

/** False until the host sets [environment] or [testMode]. */
@property (readonly) BOOL environmentChosen __attribute__((swift_name("environmentChosen")));
@property BOOL exportInterstitialEnabled __attribute__((swift_name("exportInterstitialEnabled")));
@property BOOL exportSuccessNativeEnabled __attribute__((swift_name("exportSuccessNativeEnabled")));
@property BOOL homeNativeEnabled __attribute__((swift_name("homeNativeEnabled")));
@property int64_t initialRetryDelayMs __attribute__((swift_name("initialRetryDelayMs")));
@property int32_t interstitialAdExpiryMinutes __attribute__((swift_name("interstitialAdExpiryMinutes")));
@property int64_t interstitialCooldownMs __attribute__((swift_name("interstitialCooldownMs")));
@property WCALogLevel *logLevel __attribute__((swift_name("logLevel")));
@property int32_t maxRetries __attribute__((swift_name("maxRetries")));
@property int32_t minBackgroundSecondsForAppOpen __attribute__((swift_name("minBackgroundSecondsForAppOpen")));
@property int32_t nativeAdCacheBatchSize __attribute__((swift_name("nativeAdCacheBatchSize")));
@property void (^ _Nullable onAdClicked)(NSString *, NSString *) __attribute__((swift_name("onAdClicked")));
@property void (^ _Nullable onAdDismissed)(NSString *, NSString *) __attribute__((swift_name("onAdDismissed")));

/** Why a request was refused or not filled, e.g. ("interstitial", "export_interstitial", PLACEMENT_NOT_FOUND). */
@property void (^ _Nullable onAdError)(NSString *, NSString *, WCAAdError *) __attribute__((swift_name("onAdError")));

/** Every step of every request, with network, timing and policy revision. See [AdEvent]. */
@property void (^ _Nullable onAdEvent)(WCAAdEvent *) __attribute__((swift_name("onAdEvent")));
@property void (^ _Nullable onAdFailed)(NSString *, NSString *, WCAInt *, NSString *) __attribute__((swift_name("onAdFailed")));
@property void (^ _Nullable onAdImpression)(NSString *, NSString *) __attribute__((swift_name("onAdImpression")));

/** Reports which network filled a placement, e.g. ("interstitial", "export_interstitial", "applovin"). */
@property void (^ _Nullable onAdLoaded)(NSString *, NSString *, NSString *) __attribute__((swift_name("onAdLoaded")));
@property void (^ _Nullable onAdRevenue)(NSString *, WCALong *, NSString *) __attribute__((swift_name("onAdRevenue")));
@property WCABoolean *(^ _Nullable placementEnabledCheck)(NSString *) __attribute__((swift_name("placementEnabledCheck")));
@property (readonly) BOOL policyReady __attribute__((swift_name("policyReady")));
@property id<WCAPolicySource> _Nullable policySource __attribute__((swift_name("policySource")));
@property int64_t postAppOpenCooldownMs __attribute__((swift_name("postAppOpenCooldownMs")));
@property WCABoolean *(^ _Nullable premiumCheck)(void) __attribute__((swift_name("premiumCheck")));

/** Production requires an accepted policy before any request can be sent. */
@property BOOL requireRemotePolicyForProduction __attribute__((swift_name("requireRemotePolicyForProduction")));
@property int32_t rewardedAdExpiryMinutes __attribute__((swift_name("rewardedAdExpiryMinutes")));
@property BOOL rewardedBgRemovalEnabled __attribute__((swift_name("rewardedBgRemovalEnabled")));
@property int64_t rewardedCooldownMs __attribute__((swift_name("rewardedCooldownMs")));
@property BOOL settingsBannerEnabled __attribute__((swift_name("settingsBannerEnabled")));
@property BOOL showNativeShimmer __attribute__((swift_name("showNativeShimmer")));
@property BOOL tagForUnderAge __attribute__((swift_name("tagForUnderAge")));
@property BOOL templatesNativeEnabled __attribute__((swift_name("templatesNativeEnabled")));
@property NSArray<NSString *> *testDeviceIds __attribute__((swift_name("testDeviceIds")));

/**
 * Backward-compatible property for testMode.
 * Setting true puts environment in TEST; setting false keeps the safe OFF
 * state. Hosts must opt into PRODUCTION explicitly with setEnvironment.
 */
@property BOOL testMode __attribute__((swift_name("testMode")));
@end

__attribute__((swift_name("KotlinComparable")))
@protocol WCAKotlinComparable
@required
- (int32_t)compareToOther:(id _Nullable)other __attribute__((swift_name("compareTo(other:)")));
@end

__attribute__((swift_name("KotlinEnum")))
@interface WCAKotlinEnum<E> : WCABase <WCAKotlinComparable>
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) WCAKotlinEnumCompanion *companion __attribute__((swift_name("companion")));
- (int32_t)compareToOther:(E)other __attribute__((swift_name("compareTo(other:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t ordinal __attribute__((swift_name("ordinal")));
@end


/**
 * Execution environment for the WebsCareAds engine.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEnvironment")))
@interface WCAAdEnvironment : WCAKotlinEnum<WCAAdEnvironment *>
+ (instancetype)alloc __attribute__((unavailable));

/**
 * Execution environment for the WebsCareAds engine.
 */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) WCAAdEnvironment *off __attribute__((swift_name("off")));
@property (class, readonly) WCAAdEnvironment *test __attribute__((swift_name("test")));
@property (class, readonly) WCAAdEnvironment *production __attribute__((swift_name("production")));
+ (WCAKotlinArray<WCAAdEnvironment *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCAAdEnvironment *> *entries __attribute__((swift_name("entries")));
@end


/** Why the library declined or failed a request. */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdError")))
@interface WCAAdError : WCAKotlinEnum<WCAAdError *>
+ (instancetype)alloc __attribute__((unavailable));

/** Why the library declined or failed a request. */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) WCAAdError *notInitialized __attribute__((swift_name("notInitialized")));
@property (class, readonly) WCAAdError *adsDisabled __attribute__((swift_name("adsDisabled")));
@property (class, readonly) WCAAdError *placementNotFound __attribute__((swift_name("placementNotFound")));
@property (class, readonly) WCAAdError *formatMismatch __attribute__((swift_name("formatMismatch")));
@property (class, readonly) WCAAdError *placementDisabled __attribute__((swift_name("placementDisabled")));
@property (class, readonly) WCAAdError *frequencyCapped __attribute__((swift_name("frequencyCapped")));
@property (class, readonly) WCAAdError *consentRequired __attribute__((swift_name("consentRequired")));
@property (class, readonly) WCAAdError *premiumUser __attribute__((swift_name("premiumUser")));
@property (class, readonly) WCAAdError *offline __attribute__((swift_name("offline")));
@property (class, readonly) WCAAdError *noEligibleNetwork __attribute__((swift_name("noEligibleNetwork")));
@property (class, readonly) WCAAdError *noFill __attribute__((swift_name("noFill")));
@property (class, readonly) WCAAdError *showFailed __attribute__((swift_name("showFailed")));
+ (WCAKotlinArray<WCAAdError *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCAAdError *> *entries __attribute__((swift_name("entries")));
@end


/**
 * One step of an ad request, in order:
 * [Requested] → [Attempt] per network → [Loaded] or [Failed] → [Impression] → [Dismissed].
 *
 * Set `AdConfig.onAdEvent` to forward these to analytics. [revision] is the
 * Firebase policy revision in force, so a change in fill or latency can be
 * traced to the config that caused it.
 */
__attribute__((swift_name("AdEvent")))
@interface WCAAdEvent : WCABase
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Attempt")))
@interface WCAAdEventAttempt : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network outcome:(WCAAttemptOutcome *)outcome latencyMs:(int64_t)latencyMs detail:(NSString * _Nullable)detail __attribute__((swift_name("init(placement:format:revision:network:outcome:latencyMs:detail:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventAttempt *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network outcome:(WCAAttemptOutcome *)outcome latencyMs:(int64_t)latencyMs detail:(NSString * _Nullable)detail __attribute__((swift_name("doCopy(placement:format:revision:network:outcome:latencyMs:detail:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));

/** The network's own message for a no-fill, when it gave one. */
@property (readonly) NSString * _Nullable detail __attribute__((swift_name("detail")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) int64_t latencyMs __attribute__((swift_name("latencyMs")));
@property (readonly) WCAAdProviderType *network __attribute__((swift_name("network")));
@property (readonly) WCAAttemptOutcome *outcome __attribute__((swift_name("outcome")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Clicked")))
@interface WCAAdEventClicked : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network __attribute__((swift_name("init(placement:format:revision:network:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventClicked *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network __attribute__((swift_name("doCopy(placement:format:revision:network:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) WCAAdProviderType *network __attribute__((swift_name("network")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Dismissed")))
@interface WCAAdEventDismissed : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network __attribute__((swift_name("init(placement:format:revision:network:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventDismissed *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network __attribute__((swift_name("doCopy(placement:format:revision:network:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) WCAAdProviderType *network __attribute__((swift_name("network")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Failed")))
@interface WCAAdEventFailed : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision error:(WCAAdError *)error latencyMs:(int64_t)latencyMs __attribute__((swift_name("init(placement:format:revision:error:latencyMs:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventFailed *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision error:(WCAAdError *)error latencyMs:(int64_t)latencyMs __attribute__((swift_name("doCopy(placement:format:revision:error:latencyMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) WCAAdError *error __attribute__((swift_name("error")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) int64_t latencyMs __attribute__((swift_name("latencyMs")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Impression")))
@interface WCAAdEventImpression : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network __attribute__((swift_name("init(placement:format:revision:network:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventImpression *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network __attribute__((swift_name("doCopy(placement:format:revision:network:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) WCAAdProviderType *network __attribute__((swift_name("network")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Loaded")))
@interface WCAAdEventLoaded : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network latencyMs:(int64_t)latencyMs __attribute__((swift_name("init(placement:format:revision:network:latencyMs:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventLoaded *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network latencyMs:(int64_t)latencyMs __attribute__((swift_name("doCopy(placement:format:revision:network:latencyMs:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));

/** From [Requested] to the fill, across every attempt. */
@property (readonly) int64_t latencyMs __attribute__((swift_name("latencyMs")));
@property (readonly) WCAAdProviderType *network __attribute__((swift_name("network")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Paid")))
@interface WCAAdEventPaid : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network valueMicros:(int64_t)valueMicros currencyCode:(NSString *)currencyCode __attribute__((swift_name("init(placement:format:revision:network:valueMicros:currencyCode:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventPaid *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision network:(WCAAdProviderType *)network valueMicros:(int64_t)valueMicros currencyCode:(NSString *)currencyCode __attribute__((swift_name("doCopy(placement:format:revision:network:valueMicros:currencyCode:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *currencyCode __attribute__((swift_name("currencyCode")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) WCAAdProviderType *network __attribute__((swift_name("network")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@property (readonly) int64_t valueMicros __attribute__((swift_name("valueMicros")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdEvent.Requested")))
@interface WCAAdEventRequested : WCAAdEvent
- (instancetype)initWithPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision __attribute__((swift_name("init(placement:format:revision:)"))) __attribute__((objc_designated_initializer));
- (WCAAdEventRequested *)doCopyPlacement:(NSString *)placement format:(WCAAdFormat *)format revision:(int64_t)revision __attribute__((swift_name("doCopy(placement:format:revision:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) WCAAdFormat *format __attribute__((swift_name("format")));
@property (readonly) NSString *placement __attribute__((swift_name("placement")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@end


/** Ad formats a placement can request. */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdFormat")))
@interface WCAAdFormat : WCAKotlinEnum<WCAAdFormat *>
+ (instancetype)alloc __attribute__((unavailable));

/** Ad formats a placement can request. */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) WCAAdFormatCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) WCAAdFormat *appOpen __attribute__((swift_name("appOpen")));
@property (class, readonly) WCAAdFormat *banner __attribute__((swift_name("banner")));
@property (class, readonly) WCAAdFormat *native __attribute__((swift_name("native")));
@property (class, readonly) WCAAdFormat *interstitial __attribute__((swift_name("interstitial")));
@property (class, readonly) WCAAdFormat *rewarded __attribute__((swift_name("rewarded")));
@property (class, readonly) WCAAdFormat *rewardedInterstitial __attribute__((swift_name("rewardedInterstitial")));
+ (WCAKotlinArray<WCAAdFormat *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCAAdFormat *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *key __attribute__((swift_name("key")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdFormat.Companion")))
@interface WCAAdFormatCompanion : WCABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) WCAAdFormatCompanion *shared __attribute__((swift_name("shared")));
- (WCAAdFormat * _Nullable)fromKeyValue:(NSString *)value __attribute__((swift_name("fromKey(value:)")));
@end


/** Network-level settings delivered by the app's Remote Config policy. */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdNetworkConfig")))
@interface WCAAdNetworkConfig : WCABase
- (instancetype)initWithEnabled:(BOOL)enabled appId:(NSString * _Nullable)appId sdkKey:(NSString * _Nullable)sdkKey __attribute__((swift_name("init(enabled:appId:sdkKey:)"))) __attribute__((objc_designated_initializer));
- (WCAAdNetworkConfig *)doCopyEnabled:(BOOL)enabled appId:(NSString * _Nullable)appId sdkKey:(NSString * _Nullable)sdkKey __attribute__((swift_name("doCopy(enabled:appId:sdkKey:)")));

/** Network-level settings delivered by the app's Remote Config policy. */
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));

/** Network-level settings delivered by the app's Remote Config policy. */
- (NSUInteger)hash __attribute__((swift_name("hash()")));

/** Network-level settings delivered by the app's Remote Config policy. */
- (NSString *)description __attribute__((swift_name("description()")));

/** AdMob app ID, Meta app ID, or Unity Game ID. */
@property (readonly) NSString * _Nullable appId __attribute__((swift_name("appId")));
@property (readonly) BOOL enabled __attribute__((swift_name("enabled")));

/** AppLovin MAX SDK key. */
@property (readonly) NSString * _Nullable sdkKey __attribute__((swift_name("sdkKey")));
@end


/**
 * One named ad spot in the app, mapped to an ad unit per network. Format
 * defaults (the `formats` block) use the same type; their values fill in
 * whatever a placement leaves unset.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdPlacementConfig")))
@interface WCAAdPlacementConfig : WCABase
- (instancetype)initWithEnabled:(BOOL)enabled format:(WCAAdFormat * _Nullable)format order:(NSArray<WCAAdProviderType *> *)order units:(NSDictionary<WCAAdProviderType *, NSString *> *)units cooldownMs:(WCALong * _Nullable)cooldownMs maxPerSession:(WCAInt * _Nullable)maxPerSession maxPerDay:(WCAInt * _Nullable)maxPerDay timeoutMs:(WCALong * _Nullable)timeoutMs preload:(WCABoolean * _Nullable)preload collapsible:(NSString * _Nullable)collapsible __attribute__((swift_name("init(enabled:format:order:units:cooldownMs:maxPerSession:maxPerDay:timeoutMs:preload:collapsible:)"))) __attribute__((objc_designated_initializer));
- (WCAAdPlacementConfig *)doCopyEnabled:(BOOL)enabled format:(WCAAdFormat * _Nullable)format order:(NSArray<WCAAdProviderType *> *)order units:(NSDictionary<WCAAdProviderType *, NSString *> *)units cooldownMs:(WCALong * _Nullable)cooldownMs maxPerSession:(WCAInt * _Nullable)maxPerSession maxPerDay:(WCAInt * _Nullable)maxPerDay timeoutMs:(WCALong * _Nullable)timeoutMs preload:(WCABoolean * _Nullable)preload collapsible:(NSString * _Nullable)collapsible __attribute__((swift_name("doCopy(enabled:format:order:units:cooldownMs:maxPerSession:maxPerDay:timeoutMs:preload:collapsible:)")));

/**
 * One named ad spot in the app, mapped to an ad unit per network. Format
 * defaults (the `formats` block) use the same type; their values fill in
 * whatever a placement leaves unset.
 */
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));

/**
 * One named ad spot in the app, mapped to an ad unit per network. Format
 * defaults (the `formats` block) use the same type; their values fill in
 * whatever a placement leaves unset.
 */
- (NSUInteger)hash __attribute__((swift_name("hash()")));

/**
 * One named ad spot in the app, mapped to an ad unit per network. Format
 * defaults (the `formats` block) use the same type; their values fill in
 * whatever a placement leaves unset.
 */
- (NSString *)description __attribute__((swift_name("description()")));

/** "top" or "bottom" to request a collapsible banner where the network supports it. */
@property (readonly) NSString * _Nullable collapsible __attribute__((swift_name("collapsible")));
@property (readonly) WCALong * _Nullable cooldownMs __attribute__((swift_name("cooldownMs")));
@property (readonly) BOOL enabled __attribute__((swift_name("enabled")));

/** Format declared in Firebase; requests for another format are refused. */
@property (readonly) WCAAdFormat * _Nullable format __attribute__((swift_name("format")));

/** 0 means no cap. */
@property (readonly) WCAInt * _Nullable maxPerDay __attribute__((swift_name("maxPerDay")));

/** 0 means no cap. */
@property (readonly) WCAInt * _Nullable maxPerSession __attribute__((swift_name("maxPerSession")));

/** Overrides the policy-wide waterfall order for this placement when not empty. */
@property (readonly) NSArray<WCAAdProviderType *> *order __attribute__((swift_name("order")));

/** Fullscreen placements are preloaded after the policy arrives unless false. */
@property (readonly) WCABoolean * _Nullable preload __attribute__((swift_name("preload")));
@property (readonly) WCALong * _Nullable timeoutMs __attribute__((swift_name("timeoutMs")));
@property (readonly) NSDictionary<WCAAdProviderType *, NSString *> *units __attribute__((swift_name("units")));
@end


/**
 * Per-app ad policy fetched from the WebsCareAds Remote Config project.
 * Everything defaults to OFF so a partial or missing policy never serves ads.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdPolicy")))
@interface WCAAdPolicy : WCABase
- (instancetype)initWithSchemaVersion:(int32_t)schemaVersion revision:(int64_t)revision issuedAtMs:(int64_t)issuedAtMs validUntilMs:(int64_t)validUntilMs appKey:(NSString * _Nullable)appKey platform:(NSString *)platform globalAdsEnabled:(BOOL)globalAdsEnabled testMode:(BOOL)testMode mediationOrder:(NSArray<WCAAdProviderType *> *)mediationOrder networkConfigs:(NSDictionary<WCAAdProviderType *, WCAAdNetworkConfig *> *)networkConfigs placements:(NSDictionary<NSString *, WCAAdPlacementConfig *> *)placements formatDefaults:(NSDictionary<WCAAdFormat *, WCAAdPlacementConfig *> *)formatDefaults interstitialCooldownMs:(WCALong * _Nullable)interstitialCooldownMs appOpenCooldownMs:(WCALong * _Nullable)appOpenCooldownMs requestTimeoutMs:(WCALong * _Nullable)requestTimeoutMs maxWaterfallAttempts:(WCAInt * _Nullable)maxWaterfallAttempts maxRetries:(WCAInt * _Nullable)maxRetries retryBackoffMs:(WCALong * _Nullable)retryBackoffMs maxFullscreenPerSession:(WCAInt * _Nullable)maxFullscreenPerSession maxFullscreenPerDay:(WCAInt * _Nullable)maxFullscreenPerDay __attribute__((swift_name("init(schemaVersion:revision:issuedAtMs:validUntilMs:appKey:platform:globalAdsEnabled:testMode:mediationOrder:networkConfigs:placements:formatDefaults:interstitialCooldownMs:appOpenCooldownMs:requestTimeoutMs:maxWaterfallAttempts:maxRetries:retryBackoffMs:maxFullscreenPerSession:maxFullscreenPerDay:)"))) __attribute__((objc_designated_initializer));
@property (class, readonly, getter=companion) WCAAdPolicyCompanion *companion __attribute__((swift_name("companion")));

/** Why a placement cannot be requested as [format], or null when it can. */
- (WCAAdError * _Nullable)checkPlacementKey:(NSString *)placementKey format:(WCAAdFormat *)format __attribute__((swift_name("check(placementKey:format:)")));
- (WCAAdPolicy *)doCopySchemaVersion:(int32_t)schemaVersion revision:(int64_t)revision issuedAtMs:(int64_t)issuedAtMs validUntilMs:(int64_t)validUntilMs appKey:(NSString * _Nullable)appKey platform:(NSString *)platform globalAdsEnabled:(BOOL)globalAdsEnabled testMode:(BOOL)testMode mediationOrder:(NSArray<WCAAdProviderType *> *)mediationOrder networkConfigs:(NSDictionary<WCAAdProviderType *, WCAAdNetworkConfig *> *)networkConfigs placements:(NSDictionary<NSString *, WCAAdPlacementConfig *> *)placements formatDefaults:(NSDictionary<WCAAdFormat *, WCAAdPlacementConfig *> *)formatDefaults interstitialCooldownMs:(WCALong * _Nullable)interstitialCooldownMs appOpenCooldownMs:(WCALong * _Nullable)appOpenCooldownMs requestTimeoutMs:(WCALong * _Nullable)requestTimeoutMs maxWaterfallAttempts:(WCAInt * _Nullable)maxWaterfallAttempts maxRetries:(WCAInt * _Nullable)maxRetries retryBackoffMs:(WCALong * _Nullable)retryBackoffMs maxFullscreenPerSession:(WCAInt * _Nullable)maxFullscreenPerSession maxFullscreenPerDay:(WCAInt * _Nullable)maxFullscreenPerDay __attribute__((swift_name("doCopy(schemaVersion:revision:issuedAtMs:validUntilMs:appKey:platform:globalAdsEnabled:testMode:mediationOrder:networkConfigs:placements:formatDefaults:interstitialCooldownMs:appOpenCooldownMs:requestTimeoutMs:maxWaterfallAttempts:maxRetries:retryBackoffMs:maxFullscreenPerSession:maxFullscreenPerDay:)")));

/**
 * Per-app ad policy fetched from the WebsCareAds Remote Config project.
 * Everything defaults to OFF so a partial or missing policy never serves ads.
 */
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));

/**
 * Per-app ad policy fetched from the WebsCareAds Remote Config project.
 * Everything defaults to OFF so a partial or missing policy never serves ads.
 */
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (BOOL)isValidForExpectedAppKey:(NSString * _Nullable)expectedAppKey expectedPlatform:(NSString *)expectedPlatform nowMs:(int64_t)nowMs __attribute__((swift_name("isValidFor(expectedAppKey:expectedPlatform:nowMs:)")));
- (WCAAdNetworkConfig * _Nullable)networkProvider:(WCAAdProviderType *)provider __attribute__((swift_name("network(provider:)")));

/**
 * Resolves a placement: its own entry, with unset values taken from the
 * format default. Returns null for names this app's policy does not list,
 * so another app's or a misspelled placement never serves.
 */
- (WCAAdPlacementConfig * _Nullable)resolvePlacementPlacementKey:(NSString *)placementKey format:(WCAAdFormat *)format __attribute__((swift_name("resolvePlacement(placementKey:format:)")));

/**
 * Per-app ad policy fetched from the WebsCareAds Remote Config project.
 * Everything defaults to OFF so a partial or missing policy never serves ads.
 */
- (NSString *)description __attribute__((swift_name("description()")));

/** Networks that are switched on and have the credentials their SDK needs. */
- (NSSet<WCAAdProviderType *> *)usableNetworks __attribute__((swift_name("usableNetworks()")));

/**
 * Ordered (network, ad unit) candidates for one request. Networks are
 * dropped when disabled, missing an ad unit, or unable to serve [format],
 * and the list is cut to `max_waterfall_attempts`.
 */
- (NSArray<WCAKotlinPair<WCAAdProviderType *, NSString *> *> *)waterfallPlacementKey:(NSString *)placementKey format:(WCAAdFormat *)format supports:(WCABoolean *(^)(WCAAdProviderType *, WCAAdFormat *))supports __attribute__((swift_name("waterfall(placementKey:format:supports:)")));

/** Package name this policy was written for. Required. */
@property (readonly) NSString * _Nullable appKey __attribute__((swift_name("appKey")));
@property (readonly) WCALong * _Nullable appOpenCooldownMs __attribute__((swift_name("appOpenCooldownMs")));

/** Per-format defaults (`formats` block) merged under every placement of that format. */
@property (readonly) NSDictionary<WCAAdFormat *, WCAAdPlacementConfig *> *formatDefaults __attribute__((swift_name("formatDefaults")));
@property (readonly) BOOL globalAdsEnabled __attribute__((swift_name("globalAdsEnabled")));
@property (readonly) WCALong * _Nullable interstitialCooldownMs __attribute__((swift_name("interstitialCooldownMs")));
@property (readonly) int64_t issuedAtMs __attribute__((swift_name("issuedAtMs")));

/** 0 means no cap. */
@property (readonly) WCAInt * _Nullable maxFullscreenPerDay __attribute__((swift_name("maxFullscreenPerDay")));

/** 0 means no cap. */
@property (readonly) WCAInt * _Nullable maxFullscreenPerSession __attribute__((swift_name("maxFullscreenPerSession")));
@property (readonly) WCAInt * _Nullable maxRetries __attribute__((swift_name("maxRetries")));
@property (readonly) WCAInt * _Nullable maxWaterfallAttempts __attribute__((swift_name("maxWaterfallAttempts")));

/** Default waterfall order (`waterfall_order`, formerly `mediation_order`). */
@property (readonly) NSArray<WCAAdProviderType *> *mediationOrder __attribute__((swift_name("mediationOrder")));
@property (readonly) NSDictionary<WCAAdProviderType *, WCAAdNetworkConfig *> *networkConfigs __attribute__((swift_name("networkConfigs")));

/** Spots the app may request by name. Only these names are valid. */
@property (readonly) NSDictionary<NSString *, WCAAdPlacementConfig *> *placements __attribute__((swift_name("placements")));
@property (readonly) NSString *platform __attribute__((swift_name("platform")));
@property (readonly) WCALong * _Nullable requestTimeoutMs __attribute__((swift_name("requestTimeoutMs")));
@property (readonly) WCALong * _Nullable retryBackoffMs __attribute__((swift_name("retryBackoffMs")));
@property (readonly) int64_t revision __attribute__((swift_name("revision")));
@property (readonly) int32_t schemaVersion __attribute__((swift_name("schemaVersion")));

/** Test inventory on every network. A policy without `test_mode` stays in test mode. */
@property (readonly) BOOL testMode __attribute__((swift_name("testMode")));
@property (readonly) int64_t validUntilMs __attribute__((swift_name("validUntilMs")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdPolicy.Companion")))
@interface WCAAdPolicyCompanion : WCABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) WCAAdPolicyCompanion *shared __attribute__((swift_name("shared")));
@property (readonly) int32_t SCHEMA_VERSION __attribute__((swift_name("SCHEMA_VERSION")));
@end


/**
 * Parses one app's WebsCareAds Remote Config value (schema 4):
 *
 * ```json
 * {
 *   "schema": 4,
 *   "package_name": "com.example.app",
 *   "revision": 6,
 *   "ads_enabled": true,
 *   "test_mode": true,
 *   "waterfall_order": ["admob", "applovin"],
 *   "networks": {
 *     "admob":    { "enabled": true, "app_id": "ca-app-pub-...~..." },
 *     "applovin": { "enabled": true, "sdk_key": "..." }
 *   },
 *   "formats": {
 *     "interstitial": { "units": { "admob": "ca-app-pub-.../...", "applovin": "..." } }
 *   },
 *   "placements": {
 *     "export_interstitial": { "format": "interstitial", "units": { "admob": "ca-app-pub-.../..." } }
 *   }
 * }
 * ```
 *
 * Schema 3 values still parse: `mediation_order`, format defaults written as
 * placements named after a format, and the older per-network root layout.
 * Anything missing resolves to OFF, except `test_mode`, which defaults to test
 * inventory. Returns null when the JSON is unreadable or written for a
 * different package.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdPolicyParser")))
@interface WCAAdPolicyParser : WCABase
+ (instancetype)alloc __attribute__((unavailable));

/**
 * Parses one app's WebsCareAds Remote Config value (schema 4):
 *
 * ```json
 * {
 *   "schema": 4,
 *   "package_name": "com.example.app",
 *   "revision": 6,
 *   "ads_enabled": true,
 *   "test_mode": true,
 *   "waterfall_order": ["admob", "applovin"],
 *   "networks": {
 *     "admob":    { "enabled": true, "app_id": "ca-app-pub-...~..." },
 *     "applovin": { "enabled": true, "sdk_key": "..." }
 *   },
 *   "formats": {
 *     "interstitial": { "units": { "admob": "ca-app-pub-.../...", "applovin": "..." } }
 *   },
 *   "placements": {
 *     "export_interstitial": { "format": "interstitial", "units": { "admob": "ca-app-pub-.../..." } }
 *   }
 * }
 * ```
 *
 * Schema 3 values still parse: `mediation_order`, format defaults written as
 * placements named after a format, and the older per-network root layout.
 * Anything missing resolves to OFF, except `test_mode`, which defaults to test
 * inventory. Returns null when the JSON is unreadable or written for a
 * different package.
 */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)adPolicyParser __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) WCAAdPolicyParser *shared __attribute__((swift_name("shared")));
- (WCAAdPolicy * _Nullable)parseRaw:(NSString *)raw expectedPackage:(NSString *)expectedPackage __attribute__((swift_name("parse(raw:expectedPackage:)")));
- (NSString *)remoteConfigKeyPackageName:(NSString *)packageName __attribute__((swift_name("remoteConfigKey(packageName:)")));

/** iOS values sit next to the Android one, so an app ID shared by both stores keeps two entries. */
- (NSString *)remoteConfigKeyPackageName:(NSString *)packageName platform:(NSString *)platform __attribute__((swift_name("remoteConfigKey(packageName:platform:)")));
@end


/**
 * Ad networks the library can mediate between. [key] is the name used in the
 * WebsCareAds Remote Config JSON.
 */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdProviderType")))
@interface WCAAdProviderType : WCAKotlinEnum<WCAAdProviderType *>
+ (instancetype)alloc __attribute__((unavailable));

/**
 * Ad networks the library can mediate between. [key] is the name used in the
 * WebsCareAds Remote Config JSON.
 */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly, getter=companion) WCAAdProviderTypeCompanion *companion __attribute__((swift_name("companion")));
@property (class, readonly) WCAAdProviderType *admob __attribute__((swift_name("admob")));
@property (class, readonly) WCAAdProviderType *applovinMax __attribute__((swift_name("applovinMax")));
@property (class, readonly) WCAAdProviderType *unityAds __attribute__((swift_name("unityAds")));
@property (class, readonly) WCAAdProviderType *metaAudienceNetwork __attribute__((swift_name("metaAudienceNetwork")));
+ (WCAKotlinArray<WCAAdProviderType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCAAdProviderType *> *entries __attribute__((swift_name("entries")));
@property (readonly) NSString *key __attribute__((swift_name("key")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdProviderType.Companion")))
@interface WCAAdProviderTypeCompanion : WCABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) WCAAdProviderTypeCompanion *shared __attribute__((swift_name("shared")));
- (WCAAdProviderType * _Nullable)fromKeyValue:(NSString *)value __attribute__((swift_name("fromKey(value:)")));
@end


/** How one network attempt inside a request ended. */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AttemptOutcome")))
@interface WCAAttemptOutcome : WCAKotlinEnum<WCAAttemptOutcome *>
+ (instancetype)alloc __attribute__((unavailable));

/** How one network attempt inside a request ended. */
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) WCAAttemptOutcome *filled __attribute__((swift_name("filled")));
@property (class, readonly) WCAAttemptOutcome *noFill __attribute__((swift_name("noFill")));
@property (class, readonly) WCAAttemptOutcome *timeout __attribute__((swift_name("timeout")));
@property (class, readonly) WCAAttemptOutcome *notReady __attribute__((swift_name("notReady")));
+ (WCAKotlinArray<WCAAttemptOutcome *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCAAttemptOutcome *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("CollapsiblePosition")))
@interface WCACollapsiblePosition : WCAKotlinEnum<WCACollapsiblePosition *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) WCACollapsiblePosition *top __attribute__((swift_name("top")));
@property (class, readonly) WCACollapsiblePosition *bottom __attribute__((swift_name("bottom")));
+ (WCAKotlinArray<WCACollapsiblePosition *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCACollapsiblePosition *> *entries __attribute__((swift_name("entries")));
@end


/**
 * Interface for loading dynamic or remote monetization policies.
 */
__attribute__((swift_name("PolicySource")))
@protocol WCAPolicySource
@required

/**
 * Fetch latest policy. Callback delivers the parsed AdPolicy or null if fetch failed.
 */
- (void)fetchPolicyOnComplete:(void (^)(WCAAdPolicy * _Nullable))onComplete __attribute__((swift_name("fetchPolicy(onComplete:)")));
@end


/** Library-owned Remote Config; never configures the host's default Firebase app. */
__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("FirebasePolicySource")))
@interface WCAFirebasePolicySource : WCABase <WCAPolicySource>
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (void)fetchPolicyOnComplete:(void (^)(WCAAdPolicy * _Nullable))onComplete __attribute__((swift_name("fetchPolicy(onComplete:)")));
@property (readonly) NSString *applicationPackageName __attribute__((swift_name("applicationPackageName")));
@property (readonly) NSString *applicationParameterKey __attribute__((swift_name("applicationParameterKey")));
@property (readonly) NSString * _Nullable lastFetchError __attribute__((swift_name("lastFetchError")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("LogLevel")))
@interface WCALogLevel : WCAKotlinEnum<WCALogLevel *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) WCALogLevel *none __attribute__((swift_name("none")));
@property (class, readonly) WCALogLevel *error __attribute__((swift_name("error")));
@property (class, readonly) WCALogLevel *debug __attribute__((swift_name("debug")));
@property (class, readonly) WCALogLevel *verbose __attribute__((swift_name("verbose")));
+ (WCAKotlinArray<WCALogLevel *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCALogLevel *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("NativeSize")))
@interface WCANativeSize : WCAKotlinEnum<WCANativeSize *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) WCANativeSize *small __attribute__((swift_name("small")));
@property (class, readonly) WCANativeSize *medium __attribute__((swift_name("medium")));
@property (class, readonly) WCANativeSize *full __attribute__((swift_name("full")));
+ (WCAKotlinArray<WCANativeSize *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCANativeSize *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WebsCareAds")))
@interface WCAWebsCareAds : WCABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)websCareAds __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) WCAWebsCareAds *shared __attribute__((swift_name("shared")));
- (void)applyPolicyPolicy:(WCAAdPolicy *)policy __attribute__((swift_name("applyPolicy(policy:)")));
- (BOOL)applyPolicyJsonJson:(NSString *)json __attribute__((swift_name("applyPolicyJson(json:)")));

/**
 * GDPR and ATT Consent Flow on iOS
 */
- (void)checkConsentViewController:(UIViewController * _Nullable)viewController onReady:(void (^)(void))onReady __attribute__((swift_name("checkConsent(viewController:onReady:)")));
- (void)collapseActiveViews __attribute__((swift_name("collapseActiveViews()")));
- (void)destroyAllAds __attribute__((swift_name("destroyAllAds()")));
- (void)destroyNativeContainer:(UIView *)container __attribute__((swift_name("destroyNative(container:)")));
- (void)enableAutoAppOpenPlacement:(NSString *)placement minBackgroundSeconds:(int32_t)minBackgroundSeconds __attribute__((swift_name("enableAutoAppOpen(placement:minBackgroundSeconds:)")));

/**
 * Initialize the WebsCareAds SDK on iOS.
 */
- (void)doInitConfig:(void (^)(WCAAdConfig *))config __attribute__((swift_name("doInit(config:)")));
- (void)doInitAppOpenAdPlacement:(NSString *)placement minBackgroundSeconds:(int32_t)minBackgroundSeconds __attribute__((swift_name("doInitAppOpenAd(placement:minBackgroundSeconds:)")));
- (BOOL)isAdLoadedPlacement:(NSString *)placement __attribute__((swift_name("isAdLoaded(placement:)")));
- (BOOL)isEmergencyOff __attribute__((swift_name("isEmergencyOff()")));
- (BOOL)isPlacementEnabledPlacement:(NSString *)placement __attribute__((swift_name("isPlacementEnabled(placement:)")));
- (BOOL)isPrivacyOptionsRequired __attribute__((swift_name("isPrivacyOptionsRequired()")));
- (void)loadAndShowAppOpenPlacement:(NSString *)placement timeoutMs:(int64_t)timeoutMs onDismissed:(void (^)(void))onDismissed __attribute__((swift_name("loadAndShowAppOpen(placement:timeoutMs:onDismissed:)")));

/** As the common form; [onFailed] says why nothing showed, then [onDismissed] runs. */
- (void)loadAndShowAppOpenPlacement:(NSString *)placement timeoutMs:(int64_t)timeoutMs onFailed:(void (^ _Nullable)(WCAAdError *))onFailed onDismissed:(void (^)(void))onDismissed __attribute__((swift_name("loadAndShowAppOpen(placement:timeoutMs:onFailed:onDismissed:)")));

/**
 * App Open Ads
 */
- (void)loadAppOpenPlacement:(NSString *)placement __attribute__((swift_name("loadAppOpen(placement:)")));

/**
 * Banner Ads
 */
- (void)loadBannerContainer:(UIView *)container placement:(NSString *)placement __attribute__((swift_name("loadBanner(container:placement:)")));
- (void)loadCollapsibleBannerContainer:(UIView *)container placement:(NSString *)placement position:(WCACollapsiblePosition *)position __attribute__((swift_name("loadCollapsibleBanner(container:placement:position:)")));

/**
 * Interstitial Ads
 */
- (void)loadInterstitialPlacement:(NSString *)placement __attribute__((swift_name("loadInterstitial(placement:)")));
- (void)loadNativeContainer:(UIView *)container placement:(NSString *)placement __attribute__((swift_name("loadNative(container:placement:)")));

/**
 * Rewarded Ads
 */
- (void)loadRewardedPlacement:(NSString *)placement __attribute__((swift_name("loadRewarded(placement:)")));
- (void)preloadAppOpenPlacement:(NSString *)placement __attribute__((swift_name("preloadAppOpen(placement:)")));
- (void)preloadInterstitialPlacement:(NSString *)placement __attribute__((swift_name("preloadInterstitial(placement:)")));
- (void)preloadRewardedPlacement:(NSString *)placement __attribute__((swift_name("preloadRewarded(placement:)")));
- (void)preloadRewardedInterstitialPlacement:(NSString *)placement __attribute__((swift_name("preloadRewardedInterstitial(placement:)")));
- (void)refreshPolicyOnReady:(void (^)(void))onReady __attribute__((swift_name("refreshPolicy(onReady:)")));
- (void)requestConsentOnReady:(void (^)(WCABoolean *))onReady __attribute__((swift_name("requestConsent(onReady:)")));
- (void)setAppOpenLoadingScreenVisibleVisible:(BOOL)visible __attribute__((swift_name("setAppOpenLoadingScreenVisible(visible:)")));
- (void)setEmergencyOffOff:(BOOL)off __attribute__((swift_name("setEmergencyOff(off:)")));
- (void)setEnvironmentEnvironment:(WCAAdEnvironment *)environment __attribute__((swift_name("setEnvironment(environment:)")));
- (void)setPlacementEnabledPlacement:(NSString *)placement enabled:(BOOL)enabled __attribute__((swift_name("setPlacementEnabled(placement:enabled:)")));

/**
 * SDK Utilities & Safety Controls
 */
- (void)setPremiumEnabled:(BOOL)enabled __attribute__((swift_name("setPremium(enabled:)")));
- (void)showAppOpenPlacement:(NSString *)placement onDismissed:(void (^)(void))onDismissed __attribute__((swift_name("showAppOpen(placement:onDismissed:)")));
- (void)showConsentFormOnComplete:(void (^)(void))onComplete __attribute__((swift_name("showConsentForm(onComplete:)")));
- (void)showInterstitialPlacement:(NSString *)placement onDismissed:(void (^)(void))onDismissed __attribute__((swift_name("showInterstitial(placement:onDismissed:)")));
- (void)showRewardedPlacement:(NSString *)placement onRewarded:(void (^)(NSString *, WCAInt *))onRewarded onDismissed:(void (^)(void))onDismissed onNotReady:(void (^)(void))onNotReady __attribute__((swift_name("showRewarded(placement:onRewarded:onDismissed:onNotReady:)")));
- (void)showRewardedInterstitialPlacement:(NSString *)placement onRewarded:(void (^)(NSString *, WCAInt *))onRewarded onDismissed:(void (^)(void))onDismissed onNotReady:(void (^)(void))onNotReady __attribute__((swift_name("showRewardedInterstitial(placement:onRewarded:onDismissed:onNotReady:)")));

/** Completes after the initial policy fetch or safe fallback has settled. */
- (void)whenReadyOnReady:(void (^)(void))onReady __attribute__((swift_name("whenReady(onReady:)")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AdType")))
@interface WCAAdType : WCAKotlinEnum<WCAAdType *>
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) WCAAdType *appOpen __attribute__((swift_name("appOpen")));
@property (class, readonly) WCAAdType *banner __attribute__((swift_name("banner")));
@property (class, readonly) WCAAdType *collapsible __attribute__((swift_name("collapsible")));
@property (class, readonly) WCAAdType *interstitial __attribute__((swift_name("interstitial")));
@property (class, readonly) WCAAdType *rewarded __attribute__((swift_name("rewarded")));
@property (class, readonly) WCAAdType *rewardedInter __attribute__((swift_name("rewardedInter")));
@property (class, readonly) WCAAdType *native __attribute__((swift_name("native")));
+ (WCAKotlinArray<WCAAdType *> *)values __attribute__((swift_name("values()")));
@property (class, readonly) NSArray<WCAAdType *> *entries __attribute__((swift_name("entries")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("TestAdIds")))
@interface WCATestAdIds : WCABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)testAdIds __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) WCATestAdIds *shared __attribute__((swift_name("shared")));
@property (readonly) NSString *ANDROID_APP_ID __attribute__((swift_name("ANDROID_APP_ID")));
@property (readonly) NSString *ANDROID_APP_OPEN __attribute__((swift_name("ANDROID_APP_OPEN")));
@property (readonly) NSString *ANDROID_BANNER __attribute__((swift_name("ANDROID_BANNER")));
@property (readonly) NSString *ANDROID_COLLAPSIBLE __attribute__((swift_name("ANDROID_COLLAPSIBLE")));
@property (readonly) NSString *ANDROID_INTERSTITIAL __attribute__((swift_name("ANDROID_INTERSTITIAL")));
@property (readonly) NSString *ANDROID_NATIVE __attribute__((swift_name("ANDROID_NATIVE")));
@property (readonly) NSString *ANDROID_REWARDED __attribute__((swift_name("ANDROID_REWARDED")));
@property (readonly) NSString *ANDROID_REWARDED_INTER __attribute__((swift_name("ANDROID_REWARDED_INTER")));
@property (readonly) NSString *APP_OPEN __attribute__((swift_name("APP_OPEN")));
@property (readonly) NSString *BANNER __attribute__((swift_name("BANNER")));
@property (readonly) NSString *COLLAPSIBLE __attribute__((swift_name("COLLAPSIBLE")));
@property (readonly) NSString *INTERSTITIAL __attribute__((swift_name("INTERSTITIAL")));
@property (readonly) NSString *IOS_APP_ID __attribute__((swift_name("IOS_APP_ID")));
@property (readonly) NSString *IOS_APP_OPEN __attribute__((swift_name("IOS_APP_OPEN")));
@property (readonly) NSString *IOS_BANNER __attribute__((swift_name("IOS_BANNER")));
@property (readonly) NSString *IOS_COLLAPSIBLE __attribute__((swift_name("IOS_COLLAPSIBLE")));
@property (readonly) NSString *IOS_INTERSTITIAL __attribute__((swift_name("IOS_INTERSTITIAL")));
@property (readonly) NSString *IOS_NATIVE __attribute__((swift_name("IOS_NATIVE")));
@property (readonly) NSString *IOS_REWARDED __attribute__((swift_name("IOS_REWARDED")));
@property (readonly) NSString *IOS_REWARDED_INTER __attribute__((swift_name("IOS_REWARDED_INTER")));
@property (readonly) NSString *NATIVE __attribute__((swift_name("NATIVE")));
@property (readonly) NSString *REWARDED __attribute__((swift_name("REWARDED")));
@property (readonly) NSString *REWARDED_INTER __attribute__((swift_name("REWARDED_INTER")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("WebsCareAdsKt")))
@interface WCAWebsCareAdsKt : WCABase

/**
 * How long, in ms, `loadAndShowAppOpen` waits for startup (policy fetch, consent, network
 * SDK init) before the app-open request goes out. A splash therefore waits at most this
 * plus its timeout.
 */
@property (class, readonly) int64_t APP_OPEN_STARTUP_ALLOWANCE_MS __attribute__((swift_name("APP_OPEN_STARTUP_ALLOWANCE_MS")));

/**
 * Default time, in ms, a splash waits in `loadAndShowAppOpen` for the app-open request
 * to fill. It is counted from when the request goes out, not from the call.
 */
@property (class, readonly) int64_t DEFAULT_APP_OPEN_WAIT_MS __attribute__((swift_name("DEFAULT_APP_OPEN_WAIT_MS")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinEnumCompanion")))
@interface WCAKotlinEnumCompanion : WCABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
@property (class, readonly, getter=shared) WCAKotlinEnumCompanion *shared __attribute__((swift_name("shared")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinArray")))
@interface WCAKotlinArray<T> : WCABase
+ (instancetype)arrayWithSize:(int32_t)size init:(T _Nullable (^)(WCAInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (T _Nullable)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (id<WCAKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(T _Nullable)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinPair")))
@interface WCAKotlinPair<__covariant A, __covariant B> : WCABase
- (instancetype)initWithFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("init(first:second:)"))) __attribute__((objc_designated_initializer));
- (WCAKotlinPair<A, B> *)doCopyFirst:(A _Nullable)first second:(B _Nullable)second __attribute__((swift_name("doCopy(first:second:)")));
- (BOOL)equalsOther:(id _Nullable)other __attribute__((swift_name("equals(other:)")));
- (int32_t)hashCode __attribute__((swift_name("hashCode()")));
- (NSString *)toString __attribute__((swift_name("toString()")));
@property (readonly) A _Nullable first __attribute__((swift_name("first")));
@property (readonly) B _Nullable second __attribute__((swift_name("second")));
@end

__attribute__((swift_name("KotlinIterator")))
@protocol WCAKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end

#pragma pop_macro("_Nullable_result")
#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
