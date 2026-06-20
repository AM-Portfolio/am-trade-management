#!/bin/bash

# Master Test Runner for AM Trade SDKs (Unix/Linux/macOS)
# This script runs both Java and Python SDK verifications

set -o pipefail

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RESET='\033[0m'
BOLD='\033[1m'

# Initialize result variables
PYTHON_RESULT=0
JAVA_RESULT=0

print_header() {
    echo ""
    echo "${BOLD}${BLUE}================================================================================${RESET}"
    echo "${BOLD}${BLUE}🚀 AM TRADE SDK - UNIFIED TEST RUNNER${RESET}"
    echo "${BOLD}${BLUE}================================================================================${RESET}"
    echo ""
}

print_section() {
    echo ""
    echo "${BOLD}${BLUE}────────────────────────────────────────────────────────────────────────────────${RESET}"
    echo "${BOLD}${BLUE}$1${RESET}"
    echo "${BOLD}${BLUE}────────────────────────────────────────────────────────────────────────────────${RESET}"
    echo ""
}

print_success() {
    echo -e "${GREEN}✅ $1${RESET}"
}

print_error() {
    echo -e "${RED}❌ $1${RESET}"
}

print_info() {
    echo -e "${YELLOW}ℹ️  $1${RESET}"
}

run_python_tests() {
    print_section "🐍 PYTHON SDK VERIFICATION"
    
    if [ ! -f "python-verifier/verify.py" ]; then
        print_error "Python verifier not found at python-verifier/verify.py"
        PYTHON_RESULT=1
        return 1
    fi
    
    print_info "Running Python SDK verification..."
    echo ""
    
    cd python-verifier
    python verify.py
    PYTHON_RESULT=$?
    cd ..
    
    echo ""
    if [ $PYTHON_RESULT -eq 0 ]; then
        print_success "Python SDK verification PASSED"
    else
        print_error "Python SDK verification FAILED"
    fi
    
    return $PYTHON_RESULT
}

run_java_tests() {
    print_section "📘 JAVA SDK VERIFICATION"
    
    if [ ! -f "../am-trade-sdk-core/pom.xml" ]; then
        print_error "Java SDK not found at ../am-trade-sdk-core"
        JAVA_RESULT=1
        return 1
    fi
    
    print_info "Running Maven tests..."
    echo ""
    
    cd ../am-trade-sdk-core
    mvn clean test -q
    JAVA_RESULT=$?
    cd ../am-trade-sdk-test
    
    echo ""
    if [ $JAVA_RESULT -eq 0 ]; then
        print_success "Java SDK verification PASSED"
    else
        print_error "Java SDK verification FAILED"
    fi
    
    return $JAVA_RESULT
}

print_summary() {
    echo ""
    echo "${BOLD}${BLUE}================================================================================${RESET}"
    echo "${BOLD}${BLUE}📊 TEST RESULTS SUMMARY${RESET}"
    echo "${BOLD}${BLUE}================================================================================${RESET}"
    echo ""
    echo "  ${BOLD}SDK         Status${RESET}"
    echo "  ─────────────────────────────────"
    
    if [ $PYTHON_RESULT -eq 0 ]; then
        echo "  Python      ${GREEN}✅ PASSED${RESET}"
    else
        echo "  Python      ${RED}❌ FAILED${RESET}"
    fi
    
    if [ $JAVA_RESULT -eq 0 ]; then
        echo "  Java        ${GREEN}✅ PASSED${RESET}"
    else
        echo "  Java        ${RED}❌ FAILED${RESET}"
    fi
    
    echo ""
    
    if [ $PYTHON_RESULT -eq 0 ] && [ $JAVA_RESULT -eq 0 ]; then
        echo "${BOLD}${GREEN}================================================================================${RESET}"
        echo "${BOLD}${GREEN}✅ ALL TESTS PASSED - SDKs are ready for use!${RESET}"
        echo "${BOLD}${GREEN}================================================================================${RESET}"
        FINAL_RESULT=0
    else
        echo "${BOLD}${RED}================================================================================${RESET}"
        echo "${BOLD}${RED}❌ SOME TESTS FAILED - Please review errors above${RESET}"
        echo "${BOLD}${RED}================================================================================${RESET}"
        FINAL_RESULT=1
    fi
    
    echo ""
}

show_menu() {
    echo ""
    echo "Choose which tests to run:"
    echo ""
    echo "  1) Run Python SDK tests only"
    echo "  2) Run Java SDK tests only"
    echo "  3) Run both (Recommended)"
    echo "  4) Exit"
    echo ""
    read -p "Enter your choice (1-4): " choice
    
    case $choice in
        1)
            run_python_tests
            print_summary
            ;;
        2)
            run_java_tests
            print_summary
            ;;
        3)
            run_python_tests
            run_java_tests
            print_summary
            ;;
        4)
            echo "Exiting..."
            exit 0
            ;;
        *)
            print_error "Invalid choice. Please enter 1-4."
            show_menu
            ;;
    esac
}

# Main execution
main() {
    # Check if in correct directory
    if [ ! -d "python-verifier" ]; then
        print_error "python-verifier directory not found"
        print_info "Please run this script from: am-trade-sdk-test directory"
        exit 1
    fi
    
    print_header
    show_menu
    
    exit $FINAL_RESULT
}

# Run main
main "$@"
