Pod::Spec.new do |s|
  s.name = 'WebsCareAds'
  s.version = '4.2.5'
  s.summary = 'WebsCareAds iOS ad integration with automatic remote policy fetching.'
  s.homepage = 'https://github.com/webscarepk/WebCareAds'
  s.license = { :type => 'Apache-2.0' }
  s.author = { 'WebsCare' => 'info@webscare.com' }
  s.source = { :git => 'https://github.com/webscarepk/WebCareAds.git', :tag => "v#{s.version}" }
  s.ios.deployment_target = '15.0'
  s.static_framework = true
  s.vendored_frameworks = 'WebsCareAds.xcframework'
  s.resources = 'WebsCareAdsPrivacy.bundle'
  s.libraries = 'c++'
  s.pod_target_xcconfig = { 'OTHER_LDFLAGS' => '$(inherited) -ObjC' }
  s.user_target_xcconfig = { 'OTHER_LDFLAGS' => '$(inherited) -ObjC' }
  s.dependency 'Google-Mobile-Ads-SDK', '13.11.0'
  s.dependency 'GoogleUserMessagingPlatform', '3.1.0'
  s.dependency 'GoogleMobileAdsMediationFacebook', '6.22.1.0'
  s.dependency 'FirebaseCore', '12.19.0'
  s.dependency 'FirebaseRemoteConfig', '12.19.0'
end
